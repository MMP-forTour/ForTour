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
        
        // �N�Ϥ�����ù������I
        // �����o�ù��ؤo
        Display display = this.getWindowManager().getDefaultDisplay();
        float screenWidth = display.getWidth();
        float screenHeight = display.getHeight() - 60;
        this.insert2Tv("�ù��ؤo�G" + screenWidth + " x " + screenHeight);

        // �p��ù������I�ѫ������ϥ�
        this.screenCenterX = screenWidth / 2f;
        this.screenCenterY = screenHeight / 2f;
        this.insert2Tv("�ù������I�G" + screenCenterX + " x " + screenCenterY);

        // ���o�Ϥ��ؤo
        //Drawable d = this.getResources().getDrawable(R.id.img);
        Drawable d = this.getResources().getDrawable(R.drawable.cover);
        int imgWidth = d.getIntrinsicWidth();
        int imgHeight = d.getIntrinsicHeight();
        this.insert2Tv("�Ϥ��ؤo�G" + imgWidth + " x " + imgHeight);

        // ��X�Ϥ����ù������I�����W���y��
        float imgX = (screenWidth - imgWidth) / 2f;
        float imgY = (screenHeight - imgHeight) / 2f;
        this.insert2Tv("�Ϥ��y�СG" + imgX + " x " + imgY);

        // �ϥ� Matrix ���ʹϤ�
        this.matrix = new Matrix();
        this.matrix.postTranslate(imgX, imgY);
        // �@�w�n�o�Ӥ~�|�ͮ�
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
        this.insert2Tv("���V�ؼШ��סG" + targetAngle);
        this.insert2Tv("�f�V�ؼШ��סG" + reverseTargetAngle);
        switch (v.getId()) {
        case R.id.clockwise:
            new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    matrix.postRotate(30, screenCenterX, screenCenterY);
                    // �@�w�n�o�Ӥ~�|�ͮ�
                    img.setImageMatrix(matrix);
                    angle += 30;
                    insert2Tv("�ثe���סG" + angle);
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
                    // �@�w�n�o�Ӥ~�|�ͮ�
                    img.setImageMatrix(matrix);
                    angle -= 30;
                    insert2Tv("�ثe���סG" + angle);
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

            // �@�w�n�o�Ӥ~�|�ͮ�

            this.img.setImageMatrix(matrix);
            this.angle += 10;
            insert2Tv("�ثe���סG" + angle);
            break;
        case R.id.turnLeft:
            this.matrix.postRotate(-10, this.screenCenterX, this.screenCenterY);


            // �@�w�n�o�Ӥ~�|�ͮ�


            this.img.setImageMatrix(matrix);
            this.angle -= 10;
            insert2Tv("�ثe���סG" + angle);
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