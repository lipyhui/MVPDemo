package com.kawakp.kp.oxygenerator.widget.plc.kawa

import com.kawakp.kp.oxygenerator.util.VerifyUtil
import com.kawakp.kp.oxygenerator.util.VerifyUtil.calcCrc16
import com.kawakp.kp.oxygenerator.widget.plc.bean.PLCResponse
import com.kawakp.kp.oxygenerator.widget.plc.bean.PLCValue
import com.kawakp.kp.oxygenerator.widget.plc.interfaces.OnPLCResponseListener
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * 创建人: penghui.li
 * 创建时间: 2017/10/23
 * 修改人:penghui.li
 * 修改时间:2017/10/23
 * 修改内容:
 *
 * 功能描述:KAWA PLC读写操作(超出缓冲区，默认丢失后加入的元件):
 * 1、读一次最多 338 个位/字(BOOL、WORD)元件、169 个双字(DWORD、REAL)元件
 * 2、写一次最多 253 个位(BOOL)元件、202 个字(WORD)元件、101 个双字(DWORD、REAL)元件
 */

class PLCManager
/**
 * 构造方法
 *
 * @param mData            协议数据域
 * @param mVerify          协议校验
 * @param mBitElementName  位元件名称列表(按顺序)
 * @param mWordElementName 字元件名称列表(按顺序)
 * @param mWordType        字元件的数据类型列表(按顺序)
 */
private constructor(
        /** 待发送数据  */
        private val mData: ByteArray,
        /** 数据校验信息  */
        private val mVerify: ByteArray,
        /** 记录BOOL类型元件名称(按顺序排序),例：X100  */
        private val mBitElementName: List<String>,
        /** 记录WORD类型元件名称(按顺序排序)，例：D100  */
        private val mWordElementName: List<String>,
        /** WORD 数据的类型列表(按顺序排序)  */
        private val mWordType: List<TYPE>) {

    /** 定义字数据类型  */
    private enum class TYPE {
        WORD,
        DWORD,
        REAL
    }

    /**
     * 异步发送数据，不关心返回
     */
    fun startAsync() {
        start().subscribe()
    }

    /**
     * 开始发送数据并接收响应数据
     *
     * @param listener 监听返回响应数据
     */
    fun start(listener: OnPLCResponseListener) {
        start().subscribe { response -> listener.onPLCResponse(response) }
    }

    /**
     * 开始发送数据并接收响应数据,以 rxJava 方式返回
     *
     * @return 返回响应数据
     */
    fun start(): Observable<PLCResponse> {
        //屏蔽读写元件为 0、校验码为空的情况
        if (mData.isEmpty() || mVerify.isEmpty()) {
            return Observable.just(mData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map { PLCResponse(-100, "未知原因失败") }
        }

        //读写元件并返回
        return Observable.just(mData)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { SocketClient.sendMsg(addBytes(mData, mData.size, mVerify, mVerify.size)) }
                .map { bytes ->
                  /*  for (i in bytes.indices) {
                        Log.e("socket_Test_response", "byte[$i] = ${Integer.toHexString(bytes[i].toInt() and 0xff)}")
                    }*/
                    analysisResponse(bytes, mBitElementName, mWordElementName, mWordType)
                }
             /*   .map { response ->
//                    Log.e("socket_Test_response", "code = ${response.respCode}, msg = ${response.respMsg}")
                    for ((key, value) in response.data) {
                        Log.e("socket_Test_response", "key = $key, value = $value")
                    }
                    response
                }*/
    }

    /**
     * 同步发送数据并接收响应数据，未切换到I/O线程执行(与调用方法处于统一线程)
     *
     * @return 返回响应数据
     */
    fun startSync(): PLCResponse {
        //屏蔽读写元件为 0、校验码为空的情况
        if (mData.isEmpty() || mVerify.isEmpty()) {
            return PLCResponse(-100, "未知原因失败")
        }

        //读写元件并返回
        val bytes = SocketClient.sendMsg(addBytes(mData, mData.size, mVerify, mVerify.size))
        return analysisResponse(bytes, mBitElementName, mWordElementName, mWordType)

  /*      val response = analysisResponse(bytes, mBitElementName, mWordElementName, mWordType)

        Log.e("socket_Test_response", "code = ${response.respCode}, msg = ${response.respMsg}")
        for ((key, value) in response.data) {
            Log.e("socket_Test_response", "key = $key, value = $value")
        }

        return response*/
    }

    /*****************************************************************************
     * 读元件构造器
     * 一次最多读 338 个位/字(BOOL、WORD)元件
     * 一次最多读 169 个双字(DWORD、REAL)元件
     *****************************************************************************/
    class ReadBuilder {
        /** 去除 2 字节的校验码长度，协议头部内容放入bits数组中  */
        private val MAX_BIT_LEN = MAX_BUFF_LEN - 2
        /** 去除 8 字节的头部长度，2 字节的校验码长度  */
        private val MAX_WORD_LEN = MAX_BUFF_LEN - 8 - 2

        /** 定义位元件数据  */
        private val bits: ByteArray
        /** 定义字元件数据  */
        private val words: ByteArray

        /** 记录BOOL类型元件名称(按顺序排序),例：X100  */
        private val bitElementName: MutableList<String>
        /** 记录WORD类型元件名称(按顺序排序)，例：D100  */
        private val wordElementName: MutableList<String>
        /** 记录需读取 WORD 数据的类型(按顺序排序)  */
        private val wordType: MutableList<TYPE>

        /** 统计添加元件的数据域(bits + words)总长度,避免总长度超过缓存区大小  */
        private var bytesCount = 0

        /** 统计要读的位元件个数  */
        private var bitCount = 0
        /** 统计要读的字元件个数  */
        private var wordCount = 0

        /** 读一个位元件需要的字节数  */
        private val singleBit = 3
        /** 读一个字元件需要的字节数  */
        private val singleWord = 3

        init {
            bits = ByteArray(MAX_BIT_LEN)
            words = ByteArray(MAX_WORD_LEN)

            bitElementName = ArrayList()
            wordElementName = ArrayList()
            wordType = ArrayList()
        }

        /**
         * 读布尔(BOOL)数据
         *
         * @param element 布尔(BOOL)元件类型
         * @param addr    元件地址
         * @return 当前建造类
         */
        fun readBool(element: Element.BOOL, addr: Int): ReadBuilder {
            //防止数据超过缓冲大小
            if (8 + bytesCount + singleBit > MAX_BIT_LEN) {
                return this
            }

            //从协议头部后开始接收位元件
            val start = bitCount * singleBit + 8
            bits[start] = element.code
            bits[start + 1] = addr.toByte()
            bits[start + 2] = (addr shr 8).toByte()

            //统计
            bitElementName.add(element.name + addr)
            bitCount++
            bytesCount += singleBit
            return this
        }

        /**
         * 读字(WORD)数据
         *
         * @param element 字(WORD)元件类型
         * @param addr    元件地址
         * @return 当前建造类
         */
        fun readWord(element: Element.WORD, addr: Int): ReadBuilder {
            //防止数据超过缓冲大小
            if (bytesCount + singleWord > MAX_WORD_LEN) {
                return this
            }

            val start = wordCount * singleWord
            words[start] = element.code
            words[start + 1] = addr.toByte()
            words[start + 2] = (addr shr 8).toByte()

            //统计
            wordElementName.add(element.name + addr)
            wordType.add(TYPE.WORD)
            wordCount++
            bytesCount += singleWord
            return this
        }

        /**
         * 读双字(DWORD)数据
         *
         * @param element 双字(DWORD)元件类型
         * @param addr    元件地址
         * @return 当前建造类
         */
        fun readDWord(element: Element.DWORD, addr: Int): ReadBuilder {
            //防止数据超过缓冲大小
            if (bytesCount + singleWord * 2 > MAX_WORD_LEN) {
                return this
            }

            val start = wordCount * singleWord
            words[start] = element.code
            words[start + 1] = addr.toByte()
            words[start + 2] = (addr shr 8).toByte()

            val addr2 = addr + 1
            words[start + 3] = element.code
            words[start + 4] = addr2.toByte()
            words[start + 5] = (addr2 shr 8).toByte()

            //统计
            wordElementName.add(element.name + addr)
            wordType.add(TYPE.DWORD)
            wordCount += 2
            bytesCount += singleWord * 2
            return this
        }

        /**
         * 读双字(REAL)数据
         *
         * @param element 双字(REAL)元件类型
         * @param addr    元件地址
         * @return 当前建造类
         */
        fun readReal(element: Element.REAL, addr: Int): ReadBuilder {
            //防止数据超过缓冲大小
            if (bytesCount + singleWord * 2 > MAX_WORD_LEN) {
                return this
            }

            val start = wordCount * singleWord
            words[start] = element.code
            words[start + 1] = addr.toByte()
            words[start + 2] = (addr shr 8).toByte()

            val addr2 = addr + 1
            words[start + 3] = element.code
            words[start + 4] = addr2.toByte()
            words[start + 5] = (addr2 shr 8).toByte()

            //统计
            wordElementName.add(element.name + addr)
            wordType.add(TYPE.REAL)
            wordCount += 2
            bytesCount += singleWord * 2
            return this
        }

        /**
         * 读取一组位(BOOL)元件
         *
         * @param bools 需要读取的位(BOOL)元件列表
         * @return 当前建造类
         */
        fun readBoolList(bools: List<Element.ElementBOOL>?): ReadBuilder {
            if (bools == null || bools.size <= 0) {
                return this
            }

            for (bool in bools) {
                readBool(bool.element, bool.addr)
            }
            return this
        }

        /**
         * 读取一组字(WORD)元件
         *
         * @param words 需要读取的字(WORD)元件列表
         * @return 当前建造类
         */
        fun readWordList(words: List<Element.ElementWORD>?): ReadBuilder {
            if (words == null || words.size <= 0) {
                return this
            }

            for (word in words) {
                readWord(word.element, word.addr)
            }
            return this
        }

        /**
         * 读取一组双字(DWORD)元件
         *
         * @param dwords 需要读取的位双字(DWORD)件列表
         * @return 当前建造类
         */
        fun readDWordList(dwords: List<Element.ElementDWORD>?): ReadBuilder {
            if (dwords == null || dwords.size <= 0) {
                return this
            }

            for (dword in dwords) {
                readDWord(dword.element, dword.addr)
            }
            return this
        }

        /**
         * 读取一组双字(REAL)元件
         *
         * @param reals 需要读取的双字(REAL)元件列表
         * @return 当前建造类
         */
        fun readRealList(reals: List<Element.ElementREAL>?): ReadBuilder {
            if (reals == null || reals.size <= 0) {
                return this
            }

            for (real in reals) {
                readReal(real.element, real.addr)
            }
            return this
        }

        /**
         * 构造 PLC 读写管理器
         *
         * @return PLC 读写管理器
         */
        fun build(): PLCManager {
            //判断是否有读元件
            if (bitCount == 0 && wordCount == 0) {
                return PLCManager(ByteArray(0), ByteArray(0), ArrayList(), ArrayList(), ArrayList())
            }

            buildHeader()
            val data = addBytes(bits, 8 + bitCount * singleBit, words, wordCount * singleWord)
            return PLCManager(data, calcCrc16(data, 1, data.size - 1), bitElementName, wordElementName, wordType)
        }

        /**
         * 构造协议头
         */
        private fun buildHeader() {
            //协议头部
            bits[0] = LOCAL_READ_START
            bits[1] = LOCAL_STATION
            bits[2] = LOCAL_READ_CODE
            bits[3] = LOCAL_SUB_CODE
            //数据长度
            val count = bitCount + wordCount
            bits[4] = count.toByte()
            bits[5] = (count shr 8).toByte()
            bits[6] = bitCount.toByte()
            bits[7] = (bitCount shr 8).toByte()
        }
    }

    /*****************************************************************************
     * 写元件构造器
     * 一次最多写 253 个位(BOOL)元件
     * 一次最多写 202 个字(WORD)元件
     * 一次最多写 101 个双字(DWORD、REAL)元件
     *****************************************************************************/
    class WriteBuilder {
        /** 去除 2 字节的校验码长度，协议头部内容放入bits数组中  */
        private val MAX_BIT_LEN = MAX_BUFF_LEN - 2
        /** 去除 8 字节的头部长度，2 字节的校验码长度  */
        private val MAX_WORD_LEN = MAX_BUFF_LEN - 8 - 2

        /** 定义位元件数据  */
        private val bits: ByteArray
        /** 定义字元件数据  */
        private val words: ByteArray

        /** 统计添加元件的数据域(bits + words)总长度,避免总长度超过缓存区大小  */
        private var bytesCount = 0

        /** 统计要写的位元件个数  */
        private var bitCount = 0
        /** 统计要写的字元件个数  */
        private var wordCount = 0

        /** 写一个位元件需要的字节数  */
        private val singleBit = 4
        /** 写一个字元件需要的字节数  */
        private val singleWord = 5

        init {
            bits = ByteArray(MAX_BIT_LEN)
            words = ByteArray(MAX_WORD_LEN)
        }

        /**
         * 写一个布尔(BOOL)类型元件
         *
         * @param element 布尔(BOOL)元件类型
         * @param addr    元件地址
         * @param value   要写入元件的值
         * @return 当前建造类
         */
        fun writeBool(element: Element.BOOL, addr: Int, value: Boolean): WriteBuilder {
            //防止数据超过缓冲大小
            if (8 + bytesCount + singleBit > MAX_BIT_LEN) {
                return this
            }

            //从协议头部后开始接收位元件
            val start = bitCount * singleBit + 8
            bits[start] = element.code
            bits[start + 1] = addr.toByte()
            bits[start + 2] = (addr shr 8).toByte()
            bits[start + 3] = (if (value) 1 else 0).toByte()

            //统计
            bitCount++
            bytesCount += singleBit
            return this
        }

        /**
         * 写一个字(WORD)类型元件
         *
         * @param element 字(WORD)元件类型
         * @param addr    元件地址
         * @param value   要写入元件的值
         * @return 当前建造类
         */
        fun writeWord(element: Element.WORD, addr: Int, value: Int): WriteBuilder {
            //防止数据超过缓冲大小
            if (bytesCount + singleWord > MAX_WORD_LEN) {
                return this
            }

            val start = wordCount * singleWord
            words[start] = element.code
            words[start + 1] = addr.toByte()
            words[start + 2] = (addr shr 8).toByte()
            words[start + 3] = value.toByte()
            words[start + 4] = (value shr 8).toByte()

            //统计
            wordCount++
            bytesCount += singleWord
            return this
        }

        /**
         * 写一个双字(DWORD)类型元件
         *
         * @param element 双字(DWORD)元件类型
         * @param addr    元件地址
         * @param value   要写入元件的值
         * @return 当前建造类
         */
        fun writeDWord(element: Element.DWORD, addr: Int, value: Int): WriteBuilder {
            //防止数据超过缓冲大小
            if (bytesCount + singleWord * 2 > MAX_WORD_LEN) {
                return this
            }

            val start = wordCount * singleWord
            words[start] = element.code
            words[start + 1] = addr.toByte()
            words[start + 2] = (addr shr 8).toByte()
            words[start + 3] = value.toByte()
            words[start + 4] = (value shr 8).toByte()

            val addr2 = addr + 1
            words[start + 5] = element.code
            words[start + 6] = addr2.toByte()
            words[start + 7] = (addr2 shr 8).toByte()
            words[start + 8] = (value shr 16).toByte()
            words[start + 9] = (value shr 24).toByte()

            //统计
            wordCount += 2
            bytesCount += singleWord * 2
            return this
        }

        /**
         * 写一个双字(REAL)类型元件
         *
         * @param element 双字(REAL)元件类型
         * @param addr    元件地址
         * @param value   要写入元件的值
         * @return 当前建造类
         */
        fun writeReal(element: Element.REAL, addr: Int, value: Float): WriteBuilder {
            //防止数据超过缓冲大小
            if (bytesCount + singleWord * 2 > MAX_WORD_LEN) {
                return this
            }

            //float 转 int
            val valueInt = java.lang.Float.floatToIntBits(value)

            val start = wordCount * singleWord
            words[start] = element.code
            words[start + 1] = addr.toByte()
            words[start + 2] = (addr shr 8).toByte()
            words[start + 3] = valueInt.toByte()
            words[start + 4] = (valueInt shr 8).toByte()

            val addr2 = addr + 1
            words[start + 5] = element.code
            words[start + 6] = addr2.toByte()
            words[start + 7] = (addr2 shr 8).toByte()
            words[start + 8] = (valueInt shr 16).toByte()
            words[start + 9] = (valueInt shr 24).toByte()

            //统计
            wordCount += 2
            bytesCount += singleWord * 2
            return this
        }

        /**
         * 写取一组位(BOOL)元件
         *
         * @param bools 需要写的位(BOOL)元件列表
         * @return 当前建造类
         */
        fun writeBoolList(bools: List<Element.ElementBOOL>?): WriteBuilder {
            if (bools == null || bools.size <= 0) {
                return this
            }

            for (bool in bools) {
                writeBool(bool.element, bool.addr, bool.value)
            }
            return this
        }

        /**
         * 写取一组字(WORD)元件
         *
         * @param words 需要写的字(WORD)元件列表
         * @return 当前建造类
         */
        fun writeWordList(words: List<Element.ElementWORD>?): WriteBuilder {
            if (words == null || words.size <= 0) {
                return this
            }

            for (word in words) {
                writeWord(word.element, word.addr, word.value)
            }
            return this
        }

        /**
         * 写取一组双字(DWORD)元件
         *
         * @param dwords 需要写的字(DWORD)元件列表
         * @return 当前建造类
         */
        fun writeDWordList(dwords: List<Element.ElementDWORD>?): WriteBuilder {
            if (dwords == null || dwords.size <= 0) {
                return this
            }

            for (dword in dwords) {
                writeDWord(dword.element, dword.addr, dword.value)
            }
            return this
        }

        /**
         * 写取一组双字(REAL)元件
         *
         * @param reals 需要写的字(REAL)元件列表
         * @return 当前建造类
         */
        fun writeRealList(reals: List<Element.ElementREAL>?): WriteBuilder {
            if (reals == null || reals.size <= 0) {
                return this
            }

            for (real in reals) {
                writeReal(real.element, real.addr, real.value)
            }
            return this
        }

        /**
         * 构造 PLC 读写管理器
         *
         * @return PLC 读写管理器
         */
        fun build(): PLCManager {
            //判断是否有写元件
            if (bitCount == 0 && wordCount == 0) {
                return PLCManager(ByteArray(0), ByteArray(0), ArrayList(), ArrayList(), ArrayList())
            }

            buildHeader()
            val data = addBytes(bits, 8 + bitCount * singleBit, words, wordCount * singleWord)
            return PLCManager(data, calcCrc16(data, 1, data.size - 1), ArrayList(), ArrayList(), ArrayList())
        }

        /**
         * 构造协议头
         */
        private fun buildHeader() {
            //协议头部
            bits[0] = LOCAL_WRITE_START
            bits[1] = LOCAL_STATION
            bits[2] = LOCAL_WRITE_CODE
            bits[3] = LOCAL_SUB_CODE
            //数据长度
            val count = bitCount + wordCount
            bits[4] = count.toByte()
            bits[5] = (count shr 8).toByte()
            bits[6] = bitCount.toByte()
            bits[7] = (bitCount shr 8).toByte()
        }
    }

    /*****************************************************************************
     * 协议头部协议代码定义
     * 缓存区大小定义
     * 数据解析和内部方法
     *****************************************************************************/
    private companion object {

        /** 本地 PLC 读起始字  */
        private val LOCAL_READ_START: Byte = 0x52
        /** 本地 PLC 读功能码  */
        private val LOCAL_READ_CODE: Byte = 0x69

        /** 本地 PLC 写起始字  */
        private val LOCAL_WRITE_START: Byte = 0x57
        /** 本地 PLC 写功能码  */
        private val LOCAL_WRITE_CODE: Byte = 0x68
        /** 本地 PLC 写位、字错误功能码  */
        private val LOCAL_WRITE_ERROR_CODE: Byte = 0xE8.toByte()

        /** 本地 PLC 读写从站地址  */
        private val LOCAL_STATION: Byte = 0x01
        /** 本地 PLC 读写子功能码  */
        private val LOCAL_SUB_CODE: Byte = 0x0B

        /** 定义PLC读写最大缓存区大小  */
        private val MAX_BUFF_LEN = 1024

        /**
         * 校验响应数据
         *
         * @param bytes 接收的响应数据
         * @return 返回校验结果
         */
        private fun verifyResponse(bytes: ByteArray): PLCResponse {
            //获得数据长度
            val length = bytes.size

            //判断 Socket 是否连接成功
            if (length <= 0) {
                return PLCResponse(-1, "连接失败")
            }

            if (length < 3){
                return PLCResponse(-3, "响应接收失败")
            }

            //crc16校验接收数据
            val crc = VerifyUtil.calcCrc16(bytes, 0, length - 2)
            if (crc[0] != bytes[length - 2] || crc[1] != bytes[length - 1]) {
                return PLCResponse(-4, "校验失败")
            } else {
                return PLCResponse(0, "成功")
            }
        }

        /**
         * 解析接收的响应数据
         *
         * @param bytes           接收的响应数据
         * @param bitElementName  位元件名称列表(按顺序)
         * @param wordElementName 字元件名称列表(按顺序)
         * @param wordType        字元件的数据类型列表(按顺序)
         * @return 返回解析结果
         */
        private fun analysisResponse(bytes: ByteArray, bitElementName: List<String>, wordElementName: List<String>,
                                     wordType: List<TYPE>): PLCResponse {
            //crc16校验
            val response = verifyResponse(bytes)

            //校验错误
            if (response.respCode < 0) {
                return response
            }

            //判断响应头部信息
            if (bytes[0] != LOCAL_STATION || bytes[2] != LOCAL_SUB_CODE) {
                response.respCode = -3
                response.respMsg = "响应接收失败"
                return response
            }

            if (bytes[1] == LOCAL_READ_CODE) {
                //根据长度判断接收响应数据是否正确
                val length = ((bytes[3].toInt() shl 8) and 0xff00) or (bytes[4].toInt() and 0xff)
                //响应数据总长度是 = 响应头部(5个字节) + 数据 + 校验码(2个字节)
                if (bytes.size != (5 + 2 + length)) {
                    response.respCode = -3
                    response.respMsg = "响应接收失败"
                    return response
                }

                //解析BOOL类型数据
                for (i in bitElementName.indices) {
                    val status = (bytes[5 + i / 8].toInt() and (0x01 shl (i % 8))) > 0
                    response.data.put(bitElementName[i], PLCValue(status))
                }

                //判断字类型数据是否可以正常解析
                if (!wordElementName.isEmpty() && !wordType.isEmpty() && wordElementName.size == wordType.size) {
                    //计算WORD类型数据起始位置
                    var startPosition = 5 + bitElementName.size / 8
                    if (bitElementName.size % 8 > 0) {
                        startPosition++
                    }

                    //解析WORD、DWORD、REAL类型数据
                    for (i in wordElementName.indices) {
                     /*   Log.e("socket_Test_response",
                                "i = $i, " + "name = ${wordElementName[i]}, " +
                                        "startPosition = $startPosition")*/
                        when (wordType[i]) {
                            //解析WORD
                            TYPE.WORD -> {
                                val value = ((bytes[startPosition].toInt() shl 8) and 0xff00) or
                                        (bytes[startPosition + 1].toInt() and 0xff)
                                response.data.put(wordElementName[i], PLCValue(false, value))
                                startPosition += 2
                            }

                            //解析DWORD
                            TYPE.DWORD -> {
                                val value = ((bytes[startPosition].toLong() shl 8) and 0xff00) or
                                        ((bytes[startPosition + 1].toLong()) and 0xff) or
                                        ((bytes[startPosition + 2].toLong() shl 24) and 0xff000000) or
                                        (bytes[startPosition + 3].toLong() shl 16 and 0xff0000)
                                response.data.put(wordElementName[i], PLCValue(false, 0, value.toInt()))
                                startPosition += 4
                            }

                            //解析REAL
                            TYPE.REAL -> {
                                val value = ((bytes[startPosition].toLong() shl 8) and 0xff00) or
                                        ((bytes[startPosition + 1].toLong()) and 0xff) or
                                        ((bytes[startPosition + 2].toLong() shl 24) and 0xff000000) or
                                        (bytes[startPosition + 3].toLong() shl 16 and 0xff0000)
                                response.data.put(wordElementName[i], PLCValue(false, 0, 0,
                                        java.lang.Float.intBitsToFloat(value.toInt())))
                                startPosition += 4
                            }
                        }
                    }
                }

                return response
            } else if (bytes[1] == LOCAL_WRITE_CODE) {
                response.respCode = 0
                response.respMsg = "成功"
                return response
            } else if (bytes[1] == LOCAL_WRITE_ERROR_CODE) {
                response.respCode = -2
                response.respMsg = "写数据失败"
                return response
            } else {
                response.respCode = -100
                response.respMsg = "未知原因失败"
                return response
            }
        }

        /**
         * 合并两个byte数组
         *
         * @param bytes1       待合并 byte 数组 1
         * @param bytes1Length 待合并 byte 数组 1 的长度，从第 0 为开始合并
         * @param bytes2       待合并 byte 数组 2
         * @param bytes2Length 待合并 byte 数组 2 的长度，从第 0 为开始合并
         * @return bytes1 与 bytes2 按相应长度拼接后的结果
         */
        private fun addBytes(bytes1: ByteArray, bytes1Length: Int, bytes2: ByteArray, bytes2Length: Int): ByteArray {

            val data = ByteArray(bytes1Length + bytes2Length)

            if (bytes1Length == 0) {    //当bytes1数组为空时，只处理bytes2数组，优化性能
                System.arraycopy(bytes2, 0, data, 0, bytes2Length)
            } else if (bytes2Length == 0) {    //当bytes2数组为空时，只处理bytes1数组，优化性能
                System.arraycopy(bytes1, 0, data, 0, bytes1Length)
            } else {
                System.arraycopy(bytes1, 0, data, 0, bytes1Length)
                System.arraycopy(bytes2, 0, data, bytes1Length, bytes2Length)
            }

            return data
        }
    }
}
