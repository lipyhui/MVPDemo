package com.kawakp.kp.oxygenerator.view.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.base.BaseFragment;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.interf.FragmentListener;
import com.kawakp.kp.oxygenerator.util.StringDecimalUtil;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link DataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataFragment extends BaseFragment {

    //@BindView(R2.id.data_plc_time)
    TextView plcTime;

    //消息
    //@BindView(R2.id.data_info)
    ImageView info;

    //当前数据
    //@BindView(R2.id.current_oo_con)
    TextView currentOOCon;
    //@BindView(R2.id.current_p)
    TextView currentP;
    //@BindView(R2.id.current_t)
    TextView currentT;
    //@BindView(R2.id.current_h)
    TextView currentH;
    //@BindView(R2.id.current_tank_p)
    TextView currentTankP;
    //@BindView(R2.id.current_ox_p)
    TextView currentOxP;

    //原始数据
    //@BindView(R2.id.original_oo_con)
    TextView originalOOCon;
    //@BindView(R2.id.original_p)
    TextView originalP;
    //@BindView(R2.id.original_t)
    TextView originalT;
    //@BindView(R2.id.original_h)
    TextView originalH;
    //@BindView(R2.id.original_tank_p)
    TextView originalTankP;
    //@BindView(R2.id.original_ox_p)
    TextView originalOxP;

    //用于切换界面和推出时取消订阅
    private CompositeDisposable composite = new CompositeDisposable();

    //判断 Fragment 是否处于隐藏状态
    //private boolean polling = true;

    private FragmentListener listener;

    public DataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_data;
    }

    @Override
    public void initView(View view) {
        plcTime = (TextView) view.findViewById(R.id.data_plc_time);
        info = (ImageView) view.findViewById(R.id.data_info);
        currentOOCon = (TextView) view.findViewById(R.id.current_oo_con);
        currentP = (TextView) view.findViewById(R.id.current_p);
        currentT = (TextView) view.findViewById(R.id.current_t);
        currentH = (TextView) view.findViewById(R.id.current_h);
        currentTankP = (TextView) view.findViewById(R.id.current_tank_p);
        currentOxP = (TextView) view.findViewById(R.id.current_ox_p);
        originalOOCon = (TextView) view.findViewById(R.id.original_oo_con);
        originalP = (TextView) view.findViewById(R.id.original_p);
        originalT = (TextView) view.findViewById(R.id.original_t);
        originalH = (TextView) view.findViewById(R.id.original_h);
        originalTankP = (TextView) view.findViewById(R.id.original_tank_p);
        originalOxP = (TextView) view.findViewById(R.id.original_ox_p);

        info.setOnClickListener(this);
    }

    @Override
    public void initData() {
        updateTime();

        updateInfo();
        updateData();
    }

    @Override
    protected void hideFragment() {
        //polling = false;

        composite.clear();
    }

    /**
     * 获取时间
     */
    private void updateTime(){
        composite.add(API.getTime()
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {
                        return objectObservable.delay(UpdateConstant.UPDATE_TIME, TimeUnit.SECONDS);
                    }
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable)
                            throws Exception {
                        return throwableObservable.delay(UpdateConstant.UPDATE_TIME, TimeUnit.SECONDS);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        plcTime.setText(s);
                    }
                }));
    }

    /**
     * 监听报警消息
     */
    private void updateInfo(){
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
                .map(new Function<Boolean[], Boolean[]>() {
                    @Override
                    public Boolean[] apply(@NonNull Boolean[] booleen) throws Exception {
                        oxygeneratorApplication.checkWarn(booleen);
                        return booleen;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean[]>() {
                    @Override
                    public void accept(@NonNull Boolean[] booleen) throws Exception {
                        if (booleen[0] || booleen[1] || booleen[2]){
                            info.setImageResource(R.drawable.icon_info_red);
                        }else {
                            info.setImageResource(R.drawable.icon_info);
                        }
                    }
                }));
    }

    /**
     * 更新显示数据
     */
    private void updateData(){
        composite.add(API.getSensorData()
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
                .map(new Function<String[], String[]>() {
                    @Override
                    public String[] apply(@NonNull String[] strings) throws Exception {
                        String[] data = new String[12];
                        //原始数据
                        data[0] = strings[0] + " %";
                        data[1] = strings[1] + " Pa";
                        data[2] = strings[2] + " ℃";
                        data[3] = strings[3] + " %";
                        data[4] = strings[4] + " Kpa";
                        data[5] = strings[5] + " Kpa";
                        //当前数据
                        data[6] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[6])/100f), 2) + " %";
                        data[7] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[7])/10f), 1) + " Pa";
                        data[8] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[2])/10f), 1) + " ℃";
                        data[9] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[3])/10f), 1) + " %";
                        data[10] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[4])/10f), 1) + " Kpa";
                        data[11] = StringDecimalUtil.limitedDecimal(String.valueOf(Integer.parseInt(strings[5])/10f), 1) + " Kpa";
                        return data;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String[]>() {
                    @Override
                    public void accept(@NonNull String[] strings) throws Exception {
                        //原始数据
                        originalOOCon.setText(strings[0]);
                        originalP.setText(strings[1]);
                        originalT.setText(strings[2]);
                        originalH.setText(strings[3]);
                        originalTankP.setText(strings[4]);
                        originalOxP.setText(strings[5]);
                        //当前数据
                        currentOOCon.setText(strings[6]);
                        currentP.setText(strings[7]);
                        currentT.setText(strings[8]);
                        currentH.setText(strings[9]);
                        currentTankP.setText(strings[10]);
                        currentOxP.setText(strings[11]);
                    }
                }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.data_info:
                if (listener != null)
                    listener.onInfo();
                break;
        }
    }

    /**
     * 设置监听
     * @param listener
     */
    public void setFragmentListener(FragmentListener listener){
        this.listener = listener;
    }
}
