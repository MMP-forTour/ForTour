package com.finproj;



import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageRotate extends Activity {
	private static String TAG = "MatrixActivity";
    private ImageView img;
    private float screenCenterX;
    private float screenCenterY;
    private Matrix matrix;
    private TextView tv;
    private int angle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_mode0);
        
        this.img = (ImageView) this.findViewById(R.id.img);
       // Bitmap snoop = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
        //img.setImageBitmap(snoop);
        
        // 將圖片移到螢幕中心點
        // 先取得螢幕尺寸
        Display display = this.getWindowManager().getDefaultDisplay();
        float screenWidth = display.getWidth();
        float screenHeight = display.getHeight() - 60;
        this.insert2Tv("螢幕尺寸：" + screenWidth + " x " + screenHeight);

        // 計算螢幕中心點供後續旋轉使用
        this.screenCenterX = screenWidth / 2f;
        this.screenCenterY = screenHeight / 2f;
        this.insert2Tv("螢幕中心點：" + screenCenterX + " x " + screenCenterY);

        // 取得圖片尺寸
        //Drawable d = this.getResources().getDrawable(R.id.img);
        Drawable d = this.getResources().getDrawable(R.drawable.cover);
        int imgWidth = d.getIntrinsicWidth();
        int imgHeight = d.getIntrinsicHeight();
        this.insert2Tv("圖片尺寸：" + imgWidth + " x " + imgHeight);

        // 算出圖片位於螢幕中心點的左上角座標
        float imgX = (screenWidth - imgWidth) / 2f;
        float imgY = (screenHeight - imgHeight) / 2f;
        this.insert2Tv("圖片座標：" + imgX + " x " + imgY);

        // 使用 Matrix 移動圖片
        this.matrix = new Matrix();
        this.matrix.postTranslate(imgX, imgY);
        // 一定要這個才會生效
        this.img.setImageMatrix(this.matrix);
        this.insert2Tv("onCreated");
    }

    public void onClick(View v) {
        final int targetAngle = this.angle;
        final int reverseTargetAngle;
        if (targetAngle >= 0) {
            reverseTargetAngle = targetAngle - 360;
        }
        else {
            reverseTargetAngle = targetAngle + 360;
        }
        this.insert2Tv("順向目標角度：" + targetAngle);
        this.insert2Tv("逆向目標角度：" + reverseTargetAngle);
        switch (v.getId()) {
        case R.id.clockwise:
            new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    matrix.postRotate(30, screenCenterX, screenCenterY);
                    // 一定要這個才會生效
                    img.setImageMatrix(matrix);
                    angle += 30;
                    insert2Tv("目前角度：" + angle);
                    if (angle % 360 != targetAngle
                            && angle % 360 != reverseTargetAngle) {
                        this.sendMessageDelayed(this.obtainMessage(), 100);
                    }
                    else {
                        angle = targetAngle;
                    }
                }
            }.sendEmptyMessage(0);
            break;
        case R.id.counterClockwise:
            new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    matrix.postRotate(-30, screenCenterX, screenCenterY);
                    // 一定要這個才會生效
                    img.setImageMatrix(matrix);
                    angle -= 30;
                    insert2Tv("目前角度：" + angle);
                    if (angle % 360 != targetAngle
                            && angle % 360 != reverseTargetAngle) {
                        this.sendMessageDelayed(this.obtainMessage(), 100);
                    }
                    else {
                        angle = targetAngle;
                    }
                }
            }.sendEmptyMessage(0);
            break;
       /* case R.id.turnRight:
            this.matrix.postRotate(10, this.screenCenterX, this.screenCenterY);

            // 一定要這個才會生效

            this.img.setImageMatrix(matrix);
            this.angle += 10;
            insert2Tv("目前角度：" + angle);
            break;
        case R.id.turnLeft:
            this.matrix.postRotate(-10, this.screenCenterX, this.screenCenterY);


            // 一定要這個才會生效


            this.img.setImageMatrix(matrix);
            this.angle -= 10;
            insert2Tv("目前角度：" + angle);
            break;*/
        }
    }

    private void insert2Tv(String msg) {
        /*if (this.tv == null) {
            this.tv = (TextView) this.findViewById(R.id.tv);
        }
        this.tv.setText(msg);
        Log.d(TAG, msg);*/
    }
}