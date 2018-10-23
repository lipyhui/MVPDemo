package com.kawakp.kp.oxygenerator.widget.plc.kawa

/**
 * 创建人: penghui.li
 * 创建时间: 2017/10/20
 * 修改人:penghui.li
 * 修改时间:2017/10/20
 * 修改内容:
 *
 * 功能描述:KAWA 元件类型管理
 */
class Element private constructor() {
    /**
     * 布尔类型元件：
     * 元件X
     * 元件Y
     * 元件M
     */
    enum class BOOL(val code: Byte) {
        X(0x01),
        Y(0x02),
        M(0x03)
    }

    /**
     * 字节类型元件:
     * 元件D
     * 元件SD
     * 元件R
     */
    enum class WORD(val code: Byte) {
        D(0x09),
        SD(0x0c),
        R(0x0d)
    }

    /**
     * 双字节类型元件:
     * 元件D
     * 元件SD
     * 元件R
     */
    enum class DWORD(val code: Byte) {
        D(0x09),
        SD(0x0c),
        R(0x0d)
    }

    /**
     * REAL类型元件:
     * 元件D
     * 元件R
     */
    enum class REAL(val code: Byte) {
        D(0x09),
        R(0x0d)
    }

    /** 创建PLC BOOL 元件 */
    class ElementBOOL {
        var element: BOOL = BOOL.X
        var addr: Int = 0
        var value: Boolean = false

        constructor(element: BOOL = BOOL.X, addr: Int = 0) {
            this.element = element
            this.addr = addr
        }

        constructor(element: BOOL = BOOL.X, addr: Int = 0, value: Boolean = false) {
            this.element = element
            this.addr = addr
            this.value = value
        }
    }

    /** 创建PLC WORD 元件 */
    class ElementWORD {
        var element: WORD = WORD.D
        var addr: Int = 0
        var value: Int = 0

        constructor(element: WORD = WORD.D, addr: Int = 0) {
            this.element = element
            this.addr = addr
        }

        constructor(element: WORD = WORD.D, addr: Int = 0, value: Int = 0) {
            this.element = element
            this.addr = addr
            this.value = value
        }
    }

    /** 创建PLC DWORD 元件 */
    class ElementDWORD {
        var element: DWORD = DWORD.D
        var addr: Int = 0
        var value: Int = 0

        constructor(element: DWORD = DWORD.D, addr: Int = 0) {
            this.element = element
            this.addr = addr
        }

        constructor(element: DWORD = DWORD.D, addr: Int = 0, value: Int = 0) {
            this.element = element
            this.addr = addr
            this.value = value
        }
    }

    /** 创建PLC REAL 元件 */
    class ElementREAL {
        var element: REAL = REAL.D
        var addr: Int = 0
        var value: Float = 0F

        constructor(element: REAL = REAL.D, addr: Int = 0) {
            this.element = element
            this.addr = addr
        }

        constructor(element: REAL = REAL.D, addr: Int = 0, value: Float = 0F) {
            this.element = element
            this.addr = addr
            this.value = value
        }
    }
}