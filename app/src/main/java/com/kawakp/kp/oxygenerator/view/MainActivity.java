package com.kawakp.kp.oxygenerator.view;

import static com.kawakp.kp.oxygenerator.constant.PageConstant.CONTROL;
import static com.kawakp.kp.oxygenerator.constant.PageConstant.DATA;
import static com.kawakp.kp.oxygenerator.constant.PageConstant.HOME;
import static com.kawakp.kp.oxygenerator.constant.PageConstant.PARAM;
import static com.kawakp.kp.oxygenerator.constant.PageConstant.PROCESS;
import static com.kawakp.kp.oxygenerator.constant.PageConstant.SET;
import static com.kawakp.kp.oxygenerator.constant.PageConstant.WARN;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.api.API;
import com.kawakp.kp.oxygenerator.base.BaseActivity;
import com.kawakp.kp.oxygenerator.base.BaseFragment;
import com.kawakp.kp.oxygenerator.constant.PageConstant;
import com.kawakp.kp.oxygenerator.constant.UpdateConstant;
import com.kawakp.kp.oxygenerator.interf.FragmentListener;
import com.kawakp.kp.oxygenerator.util.CheckUtil;
import com.kawakp.kp.oxygenerator.util.GlideUtils;
import com.kawakp.kp.oxygenerator.util.RedirectUtil;
import com.kawakp.kp.oxygenerator.view.fragment.DataFragment;
import com.kawakp.kp.oxygenerator.view.fragment.HomeFragment;
import com.kawakp.kp.oxygenerator.view.fragment.ProcessFragment;
import com.kawakp.kp.oxygenerator.view.fragment.SetFragment;
import com.kawakp.kp.oxygenerator.view.fragment.WarnFragment;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements FragmentListener {

    public static String IS_LOGIN = "isLogin";
    public static String USER_HEADER = "userHeader";
    public static String USER_NAME = "userName";

    //用户头像、昵称
    //@BindView(R2.id.header)
    ImageView header;
    //@BindView(R2.id.user_name)
    TextView name;

    //主页点击button、图标、文本
    //@BindView(R2.id.home_btn)
    View homeBtn;
    //@BindView(R2.id.home_iv)
    ImageView homeIv;
    //@BindView(R2.id.home_tv)
    TextView homeTv;

    //流程拟态点击button、图标、文本
    //@BindView(R2.id.process_btn)
    View processBtn;
    //@BindView(R2.id.process_iv)
    ImageView processIv;
    //@BindView(R2.id.process_tv)
    TextView processTv;

    //系统设置点击button、图标、文本
    //@BindView(R2.id.set_btn)
    View setBtn;
    //@BindView(R2.id.set_iv)
    ImageView setIv;
    //@BindView(R2.id.set_tv)
    TextView setTv;

    //数据显示点击button、图标、文本
    //@BindView(R2.id.data_btn)
    View dataBtn;
    //@BindView(R2.id.data_iv)
    ImageView dataIv;
    //@BindView(R2.id.data_tv)
    TextView dataTv;

    //预警记录点击button、图标、文本
    //@BindView(R2.id.warn_btn)
    View warnBtn;
    //@BindView(R2.id.warn_iv)
    ImageView warnIv;
    //@BindView(R2.id.warn_tv)
    TextView warnTv;

    //登录与退出登录点击button、图标、显示文本
    //@BindView(R2.id.login_btn)
    View loginBtn;
    //@BindView(R2.id.login_iv)
    ImageView loginIv;
    //@BindView(R2.id.login_tv)
    TextView loginTv;

    //当前显示的fragment
    private BaseFragment currentFragment;

    private  HomeFragment homeFragment;
    private  ProcessFragment processFragment;
    private  SetFragment setFragment;
    private  DataFragment dataFragment;
    private  WarnFragment warnFragment;

    //是否已经添加 HomeFragment
    private boolean addHome = false;
    //是否已经添加 ProcessFragment
    private boolean addProcess = false;
    //是否已经添加 SetFragment
    private boolean addSet = false;
    //是否已经添加 DataFragment
    private boolean addData = false;
    //是否已经添加 WarnFragment
    private boolean addWarn = false;

    private boolean isLogin = false;
    private String sHeader = "NULL";
    private String sName = "";

    /**
     * 保存侧边栏当前选择项
     *
     *  mType 0：主页
     *  mType 1：流程拟态
     *  mType 2：系统设置
     *  mType 3：数据显示
     *  mType 4：预警记录
     */
    private int mType = 0;
    /**
     * 缓存设置界面是参数设置还是控制设置
     */
    private PageConstant setType = PARAM;

    /**
     * 是否双击
     */
    private boolean doubleClick = false;

    /**
     * 判断是否处于界面切换状态
     */
    private boolean changingPage = false;

    /**
     * 页面同步和登录同步取消订阅变量
     */
    private Disposable pageDisposable;
    private Disposable priority;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onClick(View v) {
        //Log.e("MainClick", "onclick");
        //防止连续点击
        if (doubleClick)
            return;
        doubleClick = true;

        //Log.e("MainClick", "doubleClick");

        Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<Long>() {
                    private Disposable disposable;
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        doubleClick = false;
                        //Log.e("MainClick", "onNext");
                        if (null != disposable && !disposable.isDisposed())
                            disposable.isDisposed();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.isDisposed();
                    }

                    @Override
                    public void onComplete() {
                        if (null != disposable && !disposable.isDisposed())
                            disposable.isDisposed();
                    }
                });

        switch (v.getId()){
            case R.id.home_btn:
                if (mType == 0)  return;
                replaceFragment(HOME);
                API.setPage(HOME);
                break;

            case R.id.process_btn:
                if (mType == 1)  return;
                replaceFragment(PROCESS);
                API.setPage(PROCESS);
                break;

            case R.id.set_btn:
                if (mType == 2)  return;
                if (!isLogin){
                    Toast.makeText(this, "您未登录，没有操作权限！", Toast.LENGTH_SHORT).show();
                    return;
                }
                replaceFragment(SET);
                API.setPage(setType);
                break;

            case R.id.data_btn:
                if (mType == 3)  return;
                replaceFragment(PageConstant.DATA);
                API.setPage(DATA);
                break;

            case R.id.warn_btn:
                if (mType == 4)  return;
                replaceFragment(PageConstant.WARN);
                API.setPage(WARN);
                break;

            case R.id.login_btn:
                //if (loginTv.getText().equals(getResources().getString(R.string.main_no_login_btn))){
                if ("登录账号".equals(loginTv.getText())){
                    RedirectUtil.redirectToForResult(this, LoginActivity.class, RedirectUtil.BundleKey.LOGIN);
                }else {
                    setLogout();
                    if (mType == 2){ //如果在设置界面，推出登录时回到主页
                        replaceFragment(HOME);
                        API.setPage(HOME);
                    }
                }
                break;

            default:
                replaceFragment(HOME);
                API.setPage(HOME);
                break;
        }

        //延迟两秒防止界面切换和界面更新冲突
        changingPage = true;
        if (priority != null && !priority.isDisposed()){
            priority.dispose();
        }
        priority = Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        changingPage = false;
                    }
                });
    }

    /**
     * 更新同步界面
     */
    private void updatePage(){
        pageDisposable = API.getPageAndLoginStatus()
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String[]>() {
                    @Override
                    public void accept(@NonNull String[] strings) throws Exception {
                        if (1 == Integer.parseInt(strings[1])){
                            setLogin();
                        }else {
                            setLogout();
                            if (2 == mType){
                                replaceFragment(HOME);
                            }
                        }

                        switch (Integer.parseInt(strings[0])){
                            case 0:
                                if (mType == 0 || changingPage)  return;
                                replaceFragment(HOME);
                                break;

                            case 1:
                                if (mType == 1 || changingPage)  return;
                                replaceFragment(PROCESS);
                                break;

                         /*   case 2:
                                if (mType == 2)  return;
                                if (!isLogin){
                                    return;
                                }
                                replaceFragment(SET);
                                break;*/

                            case 3:
                                if (mType == 3 || changingPage)  return;
                                replaceFragment(DATA);
                                break;

                            case 4:
                                if (mType == 4 || changingPage)  return;
                                replaceFragment(WARN);
                                break;

                            case 5:
                                if (!isLogin || changingPage || (mType == 2 && setType.equals(PARAM))){
                                    return;
                                }
                                replaceFragment(PARAM);
                                break;

                            case 6:
                                if (!isLogin || changingPage || (mType == 2 && setType.equals(CONTROL))){
                                    return;
                                }
                                replaceFragment(CONTROL);
                                break;

                            default:
                                if (changingPage){
                                    return;
                                }
                                replaceFragment(HOME);
                                break;
                        }
                    }
                });
    }

    /**
     * 通过类型匹配侧边选中项
     *
     * @param page
     */
    private void replaceFragment(PageConstant page){
        clearSelectType();
        switch (page){
            case HOME:
                homeBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
                homeIv.setImageResource(R.drawable.icon_home_sel);
                homeTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
                mType = 0;
                break;

            case PROCESS:
                processBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
                processIv.setImageResource(R.drawable.icon_process_sel);
                processTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
                mType = 1;
                break;

            case SET:
                setBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
                setIv.setImageResource(R.drawable.icon_settings_sel);
                setTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
                mType = 2;
                break;

            case DATA:
                dataBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
                dataIv.setImageResource(R.drawable.icon_data_sel);
                dataTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
                mType = 3;
                break;

            case WARN:
                warnBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
                warnIv.setImageResource(R.drawable.icon_warning_sel);
                warnTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
                mType = 4;
                break;

            case PARAM:
                setBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
                setIv.setImageResource(R.drawable.icon_settings_sel);
                setTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
                mType = 2;
                setType = PARAM;
                break;

            case CONTROL:
                setBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
                setIv.setImageResource(R.drawable.icon_settings_sel);
                setTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
                mType = 2;
                setType = PageConstant.CONTROL;
                break;

            default:
                homeBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
                homeIv.setImageResource(R.drawable.icon_home_sel);
                homeTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
                mType = 0;
                break;
        }
        showFragment();
    }

    /**
     * 清除以前选择的类别的选中状态
     */
    private void clearSelectType(){
        switch (mType){
            case 0: //清除主页选中状态
                homeBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                homeIv.setImageResource(R.drawable.icon_home_nor);
                homeTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));
                break;

            case 1: //清除流程拟态选中状态
                processBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                processIv.setImageResource(R.drawable.icon_process_nor);
                processTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));
                break;

            case 2://清除系统设置选中状态
                setBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                setIv.setImageResource(R.drawable.icon_settings_nor);
                setTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));
                break;

            case 3://清除数据显示选中状态
                dataBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                dataIv.setImageResource(R.drawable.icon_data_nor);
                dataTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));
                break;

            case 4://清除预警记录选中状态
                warnBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                warnIv.setImageResource(R.drawable.icon_warning_nor);
                warnTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));
                break;

            default://清除所有项选中状态
                homeBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                homeIv.setImageResource(R.drawable.icon_home_nor);
                homeTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));

                processBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                processIv.setImageResource(R.drawable.icon_process_nor);
                processTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));

                setBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                setIv.setImageResource(R.drawable.icon_settings_nor);
                setTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));

                dataBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                dataIv.setImageResource(R.drawable.icon_data_nor);
                dataTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));

                warnBtn.setBackgroundResource(R.drawable.main_menu_nor_bg);
                warnIv.setImageResource(R.drawable.icon_warning_nor);
                warnTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelect));
                break;
        }
    }

    /**
     * 根据 mType 显示相应的 fragment
     */
    private void showFragment(){
      /*  FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.hide(currentFragment);*/
        switch (mType){
            case 0:
             /*   currentFragment = homeFragment;
                if(homeFragment.isAdded() && addHome){
                    transaction.show(currentFragment);
                }else {
                    transaction.add(R.id.main_fragment, homeFragment);
                    addHome = true;
                }*/
                showFragment(homeFragment, mType, !addHome);
                if (!addHome)
                    addHome = true;
                break;

            case 1:
             /*   currentFragment = processFragment;
                if(processFragment.isAdded() && addProcess){
                    transaction.show(currentFragment);
                }else {
                    transaction.add(R.id.main_fragment, processFragment);
                    addProcess = true;
                }*/
                showFragment(processFragment, mType, !addProcess);
                if (!addProcess)
                    addProcess = true;
                break;

            case 2:
               /* currentFragment = setFragment;
                if(setFragment.isAdded() && addSet){
                    transaction.show(currentFragment);
                }else {
                    transaction.add(R.id.main_fragment, setFragment);
                    addSet = true;
                }*/
                showFragment(setFragment, mType, !addSet);
                if (!addSet)
                    addSet = true;
                break;

            case 3:
               /* currentFragment = dataFragment;
                if(dataFragment.isAdded() && addData){
                    transaction.show(currentFragment);
                }else {
                    transaction.add(R.id.main_fragment, dataFragment);
                    addData = true;
                }*/
                showFragment(dataFragment, mType, !addData);
                if (!addData)
                    addData = true;
                break;

            case 4:
            /*    currentFragment = warnFragment;
                if(warnFragment.isAdded() && addWarn){
                    transaction.show(currentFragment);
                }else {
                    transaction.add(R.id.main_fragment, warnFragment);
                    addWarn = true;
                }*/
                showFragment(warnFragment, mType, !addWarn);
                if (!addWarn)
                    addWarn = true;
                break;

            default:
                //transaction.show(currentFragment);
                showFragment(homeFragment, mType, !addHome);
                if (!addHome)
                    addHome = true;
                break;
        }
        //transaction.commit();
    }

    /**
     * 显示 fragment
     *
     * @param f
     * @param tagPage
     * @param isFirstCreated    首次添加fragment 传 true
     */
    private void showFragment(BaseFragment f, int tagPage, boolean isFirstCreated) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (!f.isAdded() && null == getSupportFragmentManager().findFragmentByTag("TAG" + tagPage)
                && isFirstCreated) {
            if (currentFragment != null) {
                ft.hide(currentFragment).add(R.id.main_fragment, f, "TAG" + tagPage);
            } else {
                ft.add(R.id.main_fragment, f, "TAG" + tagPage);
            }
        } else { //已经加载进容器里去了....
            if (currentFragment != null) {
                ft.hide(currentFragment).show(f);
            } else {
                ft.show(f);
            }
        }
        currentFragment = f;
        if (!isFinishing()) {
            ft.commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    @Override
    public void initView() {
        header = (ImageView) findViewById(R.id.header);
        name = (TextView) findViewById(R.id.user_name);
        homeBtn = findViewById(R.id.home_btn);
        homeIv = (ImageView) findViewById(R.id.home_iv);
        homeTv = (TextView) findViewById(R.id.home_tv);
        processBtn = findViewById(R.id.process_btn);
        processIv = (ImageView) findViewById(R.id.process_iv);
        processTv = (TextView) findViewById(R.id.process_tv);
        setBtn = findViewById(R.id.set_btn);
        setIv = (ImageView) findViewById(R.id.set_iv);
        setTv = (TextView) findViewById(R.id.set_tv);
        dataBtn = findViewById(R.id.data_btn);
        setTv = (TextView) findViewById(R.id.set_tv);
        dataBtn = findViewById(R.id.data_btn);
        dataIv = (ImageView) findViewById(R.id.data_iv);
        dataTv = (TextView) findViewById(R.id.data_tv);
        warnBtn = findViewById(R.id.warn_btn);
        warnIv = (ImageView) findViewById(R.id.warn_iv);
        warnTv = (TextView) findViewById(R.id.warn_tv);
        loginBtn = findViewById(R.id.login_btn);
        loginIv = (ImageView) findViewById(R.id.login_iv);
        loginTv = (TextView) findViewById(R.id.login_tv);

        createAllFragment();
        showDefaultFragment();
        updatePage();

        homeBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
        setBtn.setOnClickListener(this);
        dataBtn.setOnClickListener(this);
        warnBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    @Override
    public void initData() {
    }

    /**
     * 设置为登出状态
     */
    private void setLogout(){
        GlideUtils.LoadCircleImageWithBorder(this, R.drawable.default_header, header);
        //name.setText(getResources().getString(R.string.main_no_login_name));
        name.setText("用户名");

        //设置登录按钮为未登录状态
        loginIv.setImageResource(R.drawable.icon_login_nor);
        //loginTv.setText(getResources().getString(R.string.main_no_login_btn));
        loginTv.setText("登录账号");

        isLogin = false;
        API.setLoginStatus(false);
    }

    /**
     * 设置为登录状态并更新用户头像和用户名
     */
    public void setLogin(){
        if (CheckUtil.isEmpty(sHeader)) {
            //GlideUtils.LoadCircleImageWithBorder(this, R.drawable.default_header, header);
            GlideUtils.LoadImageWithLocation(this, R.drawable.default_header, header);
        }else {
            //GlideUtils.LoadCircleImageWithBorder(this, R.drawable.welcome, header);
            GlideUtils.LoadImageWithLocation(this, R.drawable.user_header, header);
        }
        name.setText(sName);

        //设置登录按钮为已登录状态
        loginIv.setImageResource(R.drawable.icon_exit_nor);
        //loginTv.setText(getResources().getString(R.string.main_logged_in_btn));
        loginTv.setText("退出登录");

        isLogin = true;
        API.setLoginStatus(true);
    }

    /**
     * 创建所有需要fragment
     */
    private void createAllFragment(){
        homeFragment = HomeFragment.newInstance();
        processFragment = ProcessFragment.newInstance();
        setFragment = SetFragment.newInstance();
        dataFragment = DataFragment.newInstance();
        warnFragment = WarnFragment.newInstance();

        homeFragment.setFragmentListener(this);
        processFragment.setFragmentListener(this);
        setFragment.setFragmentListener(this);
        dataFragment.setFragmentListener(this);

  /*      FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        //主页
        transaction.add(R.id.main_fragment, homeFragment);
        transaction.hide(homeFragment);

        //流程拟态
        transaction.add(R.id.main_fragment, processFragment);
        transaction.hide(processFragment);

        //系统设置
        transaction.add(R.id.main_fragment, setFragment);
        transaction.hide(setFragment);

        //数据显示
        transaction.add(R.id.main_fragment, dataFragment);
        transaction.hide(dataFragment);

        //预警记录
        transaction.add(R.id.main_fragment, warnFragment);
        transaction.hide(warnFragment);

        transaction.commit();*/
    }

    /**
     * 显示默认{@link HomeFragment}
     */
    private void showDefaultFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        //主页
        transaction.add(R.id.main_fragment, homeFragment);
        //transaction.hide(homeFragment);

        //transaction.show(homeFragment);
        currentFragment = homeFragment;

        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RedirectUtil.BundleKey.LOGIN.getRequestCode() == requestCode && resultCode == RESULT_OK){
            if (data.getBooleanExtra(IS_LOGIN, false)){
                isLogin = true;
                sHeader = data.getStringExtra(USER_HEADER);
                sName = data.getStringExtra(USER_NAME);
                setLogin();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (pageDisposable != null && !pageDisposable.isDisposed()){
            pageDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }

    @Override
    public PageConstant getCurrentSet() {
        return setType;
    }

    @Override
    public void currentSet(PageConstant page) {
        setType = page;
        API.setPage(setType);
    }

    @Override
    public void login() {
        RedirectUtil.redirectToForResult(this, LoginActivity.class, RedirectUtil.BundleKey.LOGIN);
    }

    @Override
    public void onInfo() {
        if (mType == 4)  return;
        clearSelectType();
        warnBtn.setBackgroundResource(R.drawable.main_menu_sel_bg);
        warnIv.setImageResource(R.drawable.icon_warning_sel);
        warnTv.setTextColor(ContextCompat.getColor(this, R.color.colorMainFontSelected));
        mType = 4;
        showFragment();
        API.setPage(WARN);

        //延迟两秒防止界面切换和界面更新冲突
        changingPage = true;
        if (priority != null && !priority.isDisposed()){
            priority.dispose();
        }
        priority = Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        changingPage = false;
                    }
                });
    }

    /*@Override
    public void logout() {
        setLogout();
    }*/
}
