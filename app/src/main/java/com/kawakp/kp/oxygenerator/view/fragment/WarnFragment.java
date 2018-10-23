package com.kawakp.kp.oxygenerator.view.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.adapter.WarnDataAdapter;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.base.BaseFragment;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.db.OxDBManager;
import com.kawakp.kp.oxygenerator.db.model.Warn;
import com.kawakp.kp.oxygenerator.util.CheckUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link WarnFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WarnFragment extends BaseFragment {

/*    //搜索开始日期
    //@BindView(R2.id.date_start)
    View dateStart;
    //@BindView(R2.id.date_start_tv)
    TextView dateStartTv;

    //搜索结束日期
    //@BindView(R2.id.date_end)
    View dateEnd;
    //@BindView(R2.id.date_end_tv)
    TextView deteEndTv;

    //搜索按钮
    //@BindView(R2.id.search)
    View search;*/

    //预警中
    //@BindView(R2.id.warning)
    View warning;
    //@BindView(R2.id.warn_all)
    View warnAll;

    //@BindView(R2.id.warn_time)
    View timeTv;
    //@BindView(R2.id.warn_time_iv)
    ImageView timeIv;

    //@BindView(R2.id.warn_list_data)
    RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;

    //用于切换界面和推出时取消订阅
    private CompositeDisposable composite = new CompositeDisposable();

    //判断 Fragment 是否处于隐藏状态
    //private boolean polling = true;

    private WarnDataAdapter adapter;
    //保存未处理的预警信息
    private List<Map<String, Object>> warningData = new ArrayList<>();
    //保存所有预警记录
    private List<Map<String, Object>> allWarn = new ArrayList<>();
    //判断选中类型 1、未处理预警信息 2、所有预警信息
    private int type = -1;
    //判断是否翻转
    private boolean isReverse = false;

    public WarnFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WarnFragment newInstance() {
        WarnFragment fragment = new WarnFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_warn;
    }

    @Override
    public void initView(View view) {
        warning = view.findViewById(R.id.warning);
        warnAll = view.findViewById(R.id.warn_all);
        timeTv = view.findViewById(R.id.warn_time);
        timeIv = (ImageView) view.findViewById(R.id.warn_time_iv);
        recyclerView = (RecyclerView) view.findViewById(R.id.warn_list_data);

        layoutManager = new LinearLayoutManager(oxygeneratorApplication.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

   /*     dateStart.setOnClickListener(this);
        dateEnd.setOnClickListener(this);
        search.setOnClickListener(this);*/

        timeTv.setOnClickListener(this);
        timeIv.setOnClickListener(this);
        warning.setOnClickListener(this);
        warnAll.setOnClickListener(this);

        adapter = new WarnDataAdapter(oxygeneratorApplication.getContext());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initData() {

        //updateWarnStatus();

        //首次进入界面
        if (-1 == type) {
            //显示数据库返回的未处理警报信息
            if (oxygeneratorApplication.getWarn().size() > 0) {
                getWarn();
            } else {
                getAllWarn();
            }
        }

        updateWarnStatus();
    }

    @Override
    protected void hideFragment() {
        //polling = false;
        composite.clear();
    }

    /**
     * 获取未解除的报警信息
     */
    private void getWarn(){
        if (warningData.size() != oxygeneratorApplication.getWarn().size()){
            warningData.clear();
            allWarn.clear();
        }

        type = 1;

        adapter.clearData();

        if (warningData.size() > 0 && adapter != null){
            adapter.addData(warningData, isReverse);
            return;
        }

        composite.add(Observable.just(oxygeneratorApplication.getWarn())
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .map(new Function<List<Warn>, List<Map<String, Object>>>() {
                    @Override
                    public List<Map<String, Object>> apply(@NonNull List<Warn> warns) throws Exception {
                        if (null == warningData)
                            warningData = new ArrayList<Map<String, Object>>();

                        warningData.clear();

                        for (Warn warn:warns)
                            warningData.add(0, type2adapter(warn));
                        return warningData;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Map<String, Object>>>() {
                    @Override
                    public void accept(@NonNull List<Map<String, Object>> maps) throws Exception {
                        if (null != adapter)
                            adapter.addData(0, maps, isReverse);
                    }
                }));
    }

    /**
     * 匹配所有预警信息(已处理和未处理)
     */
    private void getAllWarn(){
        if (warningData.size() != oxygeneratorApplication.getWarn().size()){
            warningData.clear();
            allWarn.clear();
        }

        type = 2;

        adapter.clearData();

        if (allWarn.size() > 0 && adapter != null) {
            adapter.addData(0, allWarn, isReverse);
            return;
        }

        composite.add(OxDBManager.getAllWarnAsc(oxygeneratorApplication.getContext())
                .observeOn(Schedulers.newThread())
                .map(new Function<List<Warn>, List<Map<String, Object>>>() {
                    @Override
                    public List<Map<String, Object>> apply(@NonNull List<Warn> warns) throws Exception {
                        if (null == allWarn)
                            allWarn = new ArrayList<Map<String, Object>>();

                        allWarn.clear();

                        for (Warn warn:warns)
                            allWarn.add(0, type2adapter(warn));
                        return allWarn;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Map<String, Object>>>() {
                    @Override
                    public void accept(@NonNull List<Map<String, Object>> maps) throws Exception {
                        if (null != adapter)
                            adapter.addData(0, maps, isReverse);
                    }
                }));
    }

    /**
     * 列表翻转设置
     *
     * @param reverse   true:列表翻转 fals:取消列表翻转
     */
/*    private void reverse(boolean reverse){
        layoutManager.setStackFromEnd(reverse);//列表再底部开始展示，反转后由上面开始展示
        layoutManager.setReverseLayout(reverse);//列表翻转
        recyclerView.setLayoutManager(layoutManager);
        adapter.notifyDataSetChanged();
    }*/


    /**
     * 根据报警时间、类型或者 Warn 对象转化为列表显示 map 数据
     * 优先处理非空的 Warn 数据
     *
     * @param warn
     * @return
     */
    private Map<String, Object> type2adapter(Warn warn){
        if (null == warn)
            return null;

        String time = "0";
        int type = 0;
        String dispelTime = "0";

        time = warn.getTime();
        type = warn.getType();
        dispelTime = warn.getdispelTime();

        if (CheckUtil.isEmpty(time) || time.equals("0"))
            return null;

        Map<String, Object> map = new HashMap<>();

        switch (type){
            case 1:
                //map.put("type", getResources().getString(R.string.warn_p_h));
                map.put("type", "压力高");
                break;

            case 2:
                //map.put("type", getResources().getString(R.string.warn_con_h));
                map.put("type", "浓度高");
                break;

            case 3:
                //map.put("type", getResources().getString(R.string.warn_oo_l));
                map.put("type", "储氧量不足");
                break;

            default:
                return null;
        }

        map.put("time", time);
        //map.put("level", 4);
        map.put("timeRemove", dispelTime);

        return map;
    }

    /**
     * 更新预警状态
     */
    private void updateWarnStatus(){
        composite.add(API.getWarnStatus()
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                        return objectObservable.delay(UpdateConstant.UPDATE_DATA, TimeUnit.SECONDS);
                    }
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable)
                            throws Exception {
                        return throwableObservable.delay(UpdateConstant.UPDATE_DATA, TimeUnit.SECONDS);
                    }
                })
                .map(new Function<Boolean[], List<Map<String, Object>>>() {
                    @Override
                    public List<Map<String, Object>> apply(@NonNull Boolean[] booleen) throws Exception {

                        Warn warn = null;
                        if (booleen[0] && (warn = oxygeneratorApplication.addWarn2DB(1)) != null){
                            Map<String, Object> data = type2adapter(warn);
                            warningData.add(0, data);
                            allWarn.add(0, data);
                        }else if (!booleen[0]){
                            String removeTime = oxygeneratorApplication.dispelWarn(1);
                            if (removeTime != null){
                                //清除报警数据
                                Iterator<Map<String, Object>> warnIter = warningData.iterator();
                                while (warnIter.hasNext()) {
                                    Map<String, Object> warnData = warnIter.next();
                                    String dispelTime = warnData.get("timeRemove").toString();
                                    if ("压力高".equals(warnData.get("type"))
                                            && (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0"))) {
                                        warnIter.remove();
                                    }
                                }

                                //清除所有数据里的报警数据
                                for (Map<String, Object> allWarnData : allWarn) {
                                    String dispelTime = allWarnData.get("timeRemove").toString();
                                    if ("压力高".equals(allWarnData.get("type"))
                                            && (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0"))) {
                                        allWarnData.put("timeRemove", removeTime);
                                    }
                                }
                            }
                        }

                        if (booleen[1] && (warn = oxygeneratorApplication.addWarn2DB(2)) != null){
                            Map<String, Object> data = type2adapter(warn);
                            warningData.add(0, data);
                            allWarn.add(0, data);
                        }else if (!booleen[1]){
                            String removeTime = oxygeneratorApplication.dispelWarn(2);
                            if (removeTime != null){
                                //清除报警数据
                                Iterator<Map<String, Object>> warnIter = warningData.iterator();
                                while (warnIter.hasNext()) {
                                    Map<String, Object> warnData = warnIter.next();
                                    String dispelTime = warnData.get("timeRemove").toString();
                                    if ("浓度高".equals(warnData.get("type"))
                                            && (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0"))) {
                                        warnIter.remove();
                                    }
                                }

                                //清除所有数据里的报警数据
                                for (Map<String, Object> allWarnData : allWarn) {
                                    String dispelTime = allWarnData.get("timeRemove").toString();
                                    if ("浓度高".equals(allWarnData.get("type"))
                                            && (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0"))) {
                                        allWarnData.put("timeRemove", removeTime);
                                    }
                                }
                            }
                        }

                        if (booleen[2] && (warn = oxygeneratorApplication.addWarn2DB(3)) != null){
                            Map<String, Object> data = type2adapter(warn);
                            warningData.add(0, data);
                            allWarn.add(0, data);
                        } else if (!booleen[2]){
                            String removeTime = oxygeneratorApplication.dispelWarn(3);
                            if (removeTime != null){
                                //清除报警数据
                                Iterator<Map<String, Object>> warnIter = warningData.iterator();
                                while (warnIter.hasNext()) {
                                    Map<String, Object> warnData = warnIter.next();
                                    String dispelTime = warnData.get("timeRemove").toString();
                                    if ("储氧量不足".equals(warnData.get("type"))
                                            && (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0"))) {
                                        warnIter.remove();
                                    }
                                }

                                //清除所有数据里的报警数据
                                for (Map<String, Object> allWarnData : allWarn) {
                                    String dispelTime = allWarnData.get("timeRemove").toString();
                                    if ("储氧量不足".equals(allWarnData.get("type"))
                                            && (CheckUtil.isEmpty(dispelTime) || dispelTime.equals("0"))) {
                                        allWarnData.put("timeRemove", removeTime);
                                    }
                                }
                            }
                        }

                        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
                        result.addAll(type == 1 ? warningData:allWarn);
                        return result;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Map<String, Object>>>() {
                    @Override
                    public void accept(@NonNull List<Map<String, Object>> maps) throws Exception {
                        if (null != adapter) {
                            adapter.clearData();
                            adapter.addData(0, maps, isReverse);
                        }
                    }
                }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
           /* case R.id.date_start: {
                TimeUtil.initSelectTimePopuwindow(getActivity(), dateStartTv, false);
                break;
            }

            case R.id.date_end: {
                TimeUtil.initSelectTimePopuwindow(getActivity(), deteEndTv, false);
                break;
            }

            case R.id.search:
                Toast.makeText(getActivity(), "搜索！", Toast.LENGTH_SHORT).show();
                break;*/

            case R.id.warning:
                getWarn();
                break;

            case R.id.warn_all:
                getAllWarn();
                break;

            case R.id.warn_time:
            case R.id.warn_time_iv:
                if (timeIv.getTag().toString().equals("1")){
                    timeIv.setImageResource(R.drawable.icon_up);
                    timeIv.setTag(2);

                    //列表翻转
//                    reverse(true);
                    adapter.reverse();
                    isReverse = true;
                } else if (timeIv.getTag().toString().equals("2")){
                    timeIv.setImageResource(R.drawable.icon_down);
                    timeIv.setTag(1);

                    //取消列表翻转
//                    reverse(false);
                    adapter.reverse();
                    isReverse = false;
                }
                break;
        }
    }
}
