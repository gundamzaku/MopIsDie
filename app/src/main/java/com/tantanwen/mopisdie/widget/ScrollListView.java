package com.tantanwen.mopisdie.widget;

import android.content.Context;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tantanwen.mopisdie.R;
import com.tantanwen.mopisdie.utils.Config;
import com.tantanwen.mopisdie.utils.Utils;

/**
 * Created by dan on 2015/7/19.
 */
public class ScrollListView extends ListView implements OnScrollListener {

    private OnRefreshListener onRefreshListener;
    private OnLoadListener onLoadListener;
    private int firstVisibleItem;
    private int scrollState;
    private boolean isLoad;
    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;
    private LayoutInflater inflater;
    private View header;
    private ImageView arrow;
    private TextView tip;
    private TextView lastUpdate;
    private ProgressBar refreshing;
    private int headerContentInitialHeight;
    private int headerContentHeight;
    private int startY;
    private boolean isRecorded;
    private int state;

    // 定义header的四种状态和当前状态
    private static final int NONE = 0;
    private static final int PULL = 1;
    private static final int RELEASE = 2;
    private static final int REFRESHING = 3;

    // 区分PULL和RELEASE的距离的大小
    private static final int SPACE = 20;
    private boolean isFristMove = true;

    // 区分当前操作是刷新还是加载
    public static final int REFRESH = 0;
    public static final int LOAD = 1;
    public static final int LOADFIRST = 2;

    public ScrollListView(Context context) {
        super(context);
        initView(context);
    }

    public ScrollListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public ScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public ScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(Context context){

        // 设置箭头特效
        animation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(100);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(100);
        reverseAnimation.setFillAfter(true);

        inflater = LayoutInflater.from(context);
        header = inflater.inflate(R.layout.pull_to_refresh_header, null);
        arrow = (ImageView) header.findViewById(R.id.arrow);
        tip = (TextView) header.findViewById(R.id.tip);
        lastUpdate = (TextView) header.findViewById(R.id.lastUpdate);
        refreshing = (ProgressBar) header.findViewById(R.id.refreshing);

        // 为listview添加头部和尾部，并进行初始化
        headerContentInitialHeight = header.getPaddingTop();
        measureView(header);
        headerContentHeight = header.getMeasuredHeight();
        //Log.d(Config.TAG,"headerContentHeight:"+headerContentHeight);
        topPadding(-headerContentHeight);
        this.addHeaderView(header);
        this.setOnScrollListener(this);

    }

    // 用来计算header大小的。内部测量item的方法
    private void measureView(View child) {

        ViewGroup.LayoutParams p = child.getLayoutParams();

        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        //System.out.println("P宽："+p.width);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        //System.out.println("量后："+childWidthSpec);
        int lpHeight = p.height;

        int childHeightSpec;

        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        child.measure(childWidthSpec, childHeightSpec);
    }
    // 调整header的大小。其实调整的只是距离顶部的高度。
    private void topPadding(int topPadding) {
        header.setPadding(header.getPaddingLeft(), topPadding, header.getPaddingRight(), header.getPaddingBottom());
        header.invalidate();//请求重新draw(),但只会绘制调用者本身
    }

    // 下拉刷新监听
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    // 解读手势，刷新header状态
    private void whenMove(MotionEvent ev) {
        //if (!isRecorded) {
            //return;
       // }
        int tmpY = (int) ev.getY();
        int space = tmpY - startY;
        int topPadding = space - headerContentHeight;
        //Log.d(Config.TAG,"state is :"+state);
        //Log.d(Config.TAG,"tmpY is :"+tmpY);
        //Log.d(Config.TAG,"headerContentHeight is :"+headerContentHeight);
        switch (state) {
            case NONE:
                if (space > 0) {
                    state = PULL;
                    refreshHeaderViewByState();
                }
                break;
            case PULL:
                topPadding(topPadding);
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL && space > headerContentHeight + SPACE) {
                    state = RELEASE;
                    refreshHeaderViewByState();
                }
                break;
            case RELEASE:
                Log.d(Config.TAG,"topPadding is :"+topPadding);
                topPadding(topPadding);
                if (space > 0 && space < headerContentHeight + SPACE) {
                    state = PULL;
                    refreshHeaderViewByState();
                } else if (space <= 0) {
                    state = NONE;
                    refreshHeaderViewByState();
                }
                break;
        }

    }

    /**
     * 监听触摸事件，解读手势
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:   //按下
                Log.d(Config.TAG,"按下时，我的firstVisibleItem是："+firstVisibleItem);
                isFristMove = false;
                if (firstVisibleItem == 0) {
                    isRecorded = true;
                    startY = (int) ev.getY();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: //抬起
                Log.d(Config.TAG,"抬起来了：");
                if (state == PULL) {
                    state = NONE;
                    refreshHeaderViewByState();
                } else if (state == RELEASE) {  //松开状态
                    state = REFRESHING;
                    refreshHeaderViewByState();
                    onRefresh();
                }
                isRecorded = false;
                isFristMove = true;
                break;
            case MotionEvent.ACTION_MOVE:   //移动
                Log.d(Config.TAG,"开始移动");
                if(isFristMove == true){
                    if (firstVisibleItem == 0) {
                        isRecorded = true;
                        startY = (int) ev.getY();
                    }
                    Log.d(Config.TAG,"我第一次运行");
                    isFristMove = false;
                }
                whenMove(ev);
                break;
        }
        return super.onTouchEvent(ev);
    }

    // 根据当前状态，调整header
    private void refreshHeaderViewByState() {
        Log.d(Config.TAG,"动画开始："+state);
        switch (state) {
            case NONE:
                topPadding(-headerContentHeight);
                tip.setText(R.string.pull_to_refresh);
                refreshing.setVisibility(View.GONE);
                arrow.clearAnimation();
                arrow.setImageResource(R.drawable.pull_to_refresh_arrow);
                break;
            case PULL:
                arrow.setVisibility(View.VISIBLE);
                tip.setVisibility(View.VISIBLE);
                lastUpdate.setVisibility(View.VISIBLE);
                refreshing.setVisibility(View.GONE);
                tip.setText(R.string.pull_to_refresh);
                arrow.clearAnimation();
                arrow.setAnimation(reverseAnimation);
                break;
            case RELEASE:
                arrow.setVisibility(View.VISIBLE);
                tip.setVisibility(View.VISIBLE);
                lastUpdate.setVisibility(View.VISIBLE);
                refreshing.setVisibility(View.GONE);
                tip.setText(R.string.pull_to_refresh);
                tip.setText(R.string.release_to_refresh);
                arrow.clearAnimation();
                arrow.setAnimation(animation);
                break;
            case REFRESHING:
                topPadding(headerContentInitialHeight);
                refreshing.setVisibility(View.VISIBLE);
                arrow.clearAnimation();
                arrow.setVisibility(View.GONE);
                tip.setVisibility(View.GONE);
                lastUpdate.setVisibility(View.GONE);
                break;
        }
    }

    // 加载更多监听
    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public void onRefresh() {

        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
            //这里调用的是父类中的方法
        }
    }

    public void onLoad() {
        if (onLoadListener != null) {
            onLoadListener.onLoad();
            //这里调用的是父类中的方法
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        /*
        第1次：scrollState = SCROLL_STATE_TOUCH_SCROLL(1) 正在滚动
        第2次：scrollState = SCROLL_STATE_FLING(2) 手指做了抛的动作（手指离开屏幕前，用力滑了一下）
        第3次：scrollState = SCROLL_STATE_IDLE(0) 停止滚动
         */
        this.scrollState = scrollState;
        ifNeedLoad(view, scrollState);
    }

    public void ifNeedLoad(AbsListView view, int scrollState){
        if(scrollState == SCROLL_STATE_IDLE && isLoad == true){
            //手指滑动停下来了。
            onLoad();
            //加载的时候不允许再次加载
            //isLoad =  true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //行到出现在第一行的列的索引值
        //System.out.println("firstVisibleItem:"+firstVisibleItem);
        this.firstVisibleItem = firstVisibleItem;
    }

    /*
     * 定义下拉刷新接口
     */
    public interface OnRefreshListener {
        public void onRefresh();
    }

    /*
     * 定义加载更多接口
     */
    public interface OnLoadListener {
        public void onLoad();
    }

    public void onRefreshComplete(String updateTime) {
        lastUpdate.setText(this.getContext().getString(R.string.lastUpdateTime, Utils.getCurrentTime()));
        state = NONE;
        refreshHeaderViewByState();
    }

    // 用于下拉刷新结束后的回调
    public void onRefreshComplete() {
        String currentTime = Utils.getCurrentTime();
        onRefreshComplete(currentTime);
    }
}
