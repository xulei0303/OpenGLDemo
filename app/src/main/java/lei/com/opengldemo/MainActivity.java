package lei.com.opengldemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glPolygonOffset;

public class MainActivity extends Activity {

    private boolean supportsEs2;
    private GLSurfaceView glView;
    private MyRenderer5 mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkOpenGL();
        initGLSurfaceView();
    }



    public void checkOpenGL(){
        ActivityManager systemService = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo deviceConfigurationInfo = systemService.getDeviceConfigurationInfo();
        // openGL es 2.0 version.
        supportsEs2 = deviceConfigurationInfo.reqGlEsVersion>=0x2000;
        Log.d("xulei"," supportsEs2  = "+ supportsEs2);
    }


    private void initGLSurfaceView() {
        glView = (GLSurfaceView) findViewById(R.id.gl_view);
        /*
        glView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {  //RGBA
                gl.glClearColor(0.7f, 0.33f, 0.11f, 0f);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                Log.d("xulei", "width = " + width + " height = " + height);
                gl.glViewport(0, 0, width / 2, height / 2);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                gl.glClear(GL_COLOR_BUFFER_BIT);
            }
        });
        */

        //glView.setRenderer(new MyRenderer());

//        glView.setRenderer(new GLRenderer2());
//        glView.setRenderer(new MyRenderer4(this));
        mRenderer = new MyRenderer5();
//        glView.setRenderer(mRenderer);

        TextureRender textureRender = new TextureRender(this);
        glView.setRenderer(textureRender);
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(glView != null){
            glView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(glView !=null){
            glView.onResume();
            /*
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            sleep(100);

                            defaultDegree += 5;
                            mHandler.sendEmptyMessage(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.start();
            */
        }
    }

    private float defaultDegree = 0.0f;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                defaultDegree += 5;
                mRenderer.rotate(defaultDegree);
                glView.invalidate();
            }

        }
    };

    class  MyRenderer implements GLSurfaceView.Renderer{

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {  //RGBA
            gl.glClearColor(0.7f, 0f, 0.11f, 0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d("xulei", "width = " + width + " height = " + height);
            //gl.glViewport(0, 0, width / 2, height / 2);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL_COLOR_BUFFER_BIT);
        }
    }


    public class GLRenderer implements GLSurfaceView.Renderer {
        private float[] mTriangleArray = {
                0f, 1f, 0f,
                -1f, -1f, 0f,
                1f, -1f, 0f
        };
        //三角形各顶点颜色(三个顶点)
        private float[] mColor = new float[]{
                1, 1, 0, 1,
                0, 1, 1, 1,
                1, 0, 1, 1
        };
        private FloatBuffer mTriangleBuffer;
        private FloatBuffer mColorBuffer;


        public GLRenderer() {
            //点相关
            //先初始化buffer，数组的长度*4，因为一个float占4个字节
            ByteBuffer bb = ByteBuffer.allocateDirect(mTriangleArray.length * 4);
            //以本机字节顺序来修改此缓冲区的字节顺序
            bb.order(ByteOrder.nativeOrder());
            mTriangleBuffer = bb.asFloatBuffer();
            //将给定float[]数据从当前位置开始，依次写入此缓冲区
            mTriangleBuffer.put(mTriangleArray);
            //设置此缓冲区的位置。如果标记已定义并且大于新的位置，则要丢弃该标记。
            mTriangleBuffer.position(0);


            //颜色相关
            ByteBuffer bb2 = ByteBuffer.allocateDirect(mColor.length * 4);
            bb2.order(ByteOrder.nativeOrder());
            mColorBuffer = bb2.asFloatBuffer();
            mColorBuffer.put(mColor);
            mColorBuffer.position(0);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            // 清除屏幕和深度缓存
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            // 重置当前的模型观察矩阵
            gl.glLoadIdentity();

            // 允许设置顶点
            //GL10.GL_VERTEX_ARRAY顶点数组
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            // 允许设置颜色
            //GL10.GL_COLOR_ARRAY颜色数组
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            //将三角形在z轴上移动
            gl.glTranslatef(0f, 0.0f, -2.0f);

            // 设置三角形
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mTriangleBuffer);
            // 设置三角形颜色
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
            // 绘制三角形
            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);


            // 取消颜色设置
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
            // 取消顶点设置
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

            //绘制结束
            gl.glFinish();

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            float ratio = (float) width / height;
            // 设置OpenGL场景的大小,(0,0)表示窗口内部视口的左下角，(w,h)指定了视口的大小
            gl.glViewport(0, 0, width, height);
            // 设置投影矩阵
            gl.glMatrixMode(GL10.GL_PROJECTION);
            // 重置投影矩阵
            gl.glLoadIdentity();
            // 设置视口的大小
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
            //以下两句声明，以后所有的变换都是针对模型(即我们绘制的图形)
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // 设置白色为清屏
            gl.glClearColor(1, 1, 1, 1);    // 这里所谓的清屏颜色理解成背景颜色就好了

        }
    }


    public class GLRenderer2 implements GLSurfaceView.Renderer {

        float rotateTri, rotateQuad;

        private float[] mTriangleArray = {
                0f, 1f, 0f,
                -1f, -1f, 0f,
                1f, -1f, 0f
        };
        //三角形各顶点颜色(三个顶点)
            private float[] mColor = new float[]{
                    1, 1, 0, 1,
                    0, 1, 1, 1,
                    1, 0, 1, 1
            };

        //正方形的四个顶点
        private float[] mQuate = new float[]{
                1, 1, 0,
                -1, -1, 0,
                1, -1, 0,
                -1, -1, 0
        };

        private FloatBuffer mTriangleBuffer;
        private FloatBuffer mQuateBuffer;
        private FloatBuffer mColorBuffer;


            public GLRenderer2() {
                //点相关
                //先初始化buffer，数组的长度*4，因为一个float占4个字节
                ByteBuffer bb = ByteBuffer.allocateDirect(mTriangleArray.length * 4);
                //以本机字节顺序来修改此缓冲区的字节顺序
                bb.order(ByteOrder.nativeOrder());
                mTriangleBuffer = bb.asFloatBuffer();
                //将给定float[]数据从当前位置开始，依次写入此缓冲区
                mTriangleBuffer.put(mTriangleArray);
                //设置此缓冲区的位置。如果标记已定义并且大于新的位置，则要丢弃该标记。
                mTriangleBuffer.position(0);

                //颜色相关
                ByteBuffer bb2 = ByteBuffer.allocateDirect(mQuate.length * 4);
                bb2.order(ByteOrder.nativeOrder());
                mQuateBuffer = bb2.asFloatBuffer();
                mQuateBuffer.put(mQuate);
                mQuateBuffer.position(0);

                //颜色相关
                ByteBuffer bb3 = ByteBuffer.allocateDirect(mColor.length * 4);
                bb3.order(ByteOrder.nativeOrder());
                mColorBuffer = bb3.asFloatBuffer();
                mColorBuffer.put(mColor);
                mColorBuffer.position(0);

            }


        @Override
        public void onDrawFrame(GL10 gl) {
            // TODO Auto-generated method stub
            Log.d("xulei","   onDrawFrame ??");
            // 清除屏幕和深度缓存
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            // 重置当前的模型观察矩阵
//            gl.glLoadIdentity();
//
//
//            // 左移 1.5 单位，并移入屏幕 6.0
//            gl.glTranslatef(-1.5f, 0.0f, -6.0f);
//            //设置旋转
//            gl.glRotatef(rotateTri, 0.0f, 1.0f, 0.0f);
//
//            //设置定点数组
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//            //设置颜色数组
//            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//
//            gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
//            // 设置三角形顶点
//            gl.glVertexPointer(3, GL10.GL_FIXED, 0, mTriangleBuffer);
//            //绘制三角形
//            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
//
//            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
//
//            //绘制三角形结束
//            gl.glFinish();

            /***********************/
        /* 渲染正方形 */
            // 重置当前的模型观察矩阵
            gl.glLoadIdentity();

            // 左移 1.5 单位，并移入屏幕 6.0
            gl.glTranslatef(-1.5f, 0.0f, -1.0f);

            // 设置当前色为蓝色
            gl.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
            //设置旋转
            gl.glRotatef(rotateQuad, 1.0f, 1.0f, 0.0f);

            //设置和绘制正方形
            gl.glVertexPointer(3, GL10.GL_FIXED, 0, mQuateBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

            //绘制正方形结束
            gl.glFinish();

            //取消顶点数组
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

            //改变旋转的角度
            //rotateTri += 0.5f;
            rotateQuad -= 0.5f;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // TODO Auto-generated method stub

            float ratio = (float) width / height;
            //设置OpenGL场景的大小
            gl.glViewport(0, 0, width, height/2);
            //设置投影矩阵
            gl.glMatrixMode(GL10.GL_PROJECTION);
            //重置投影矩阵
            gl.glLoadIdentity();
            // 设置视口的大小
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
            // 选择模型观察矩阵
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            // 重置模型观察矩阵
            gl.glLoadIdentity();

        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // TODO Auto-generated method stub
            // 启用阴影平滑
            gl.glShadeModel(GL10.GL_SMOOTH);

            // 黑色背景
            gl.glClearColor(0, 0, 0, 0);

            // 设置深度缓存
            gl.glClearDepthf(1.0f);
            // 启用深度测试
            gl.glEnable(GL10.GL_DEPTH_TEST);
            // 所作深度测试的类型
            gl.glDepthFunc(GL10.GL_LEQUAL);

            // 告诉系统对透视进行修正
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        }
    }



    // 画一个圆吧, 我们直到 openGles只能画 点 线 三角形， 那么话画圆则不是那么好画的
   public class MyRenderer4 implements GLSurfaceView.Renderer {

        private Context context;

        public MyRenderer4(Context context) {
            this.context = context;
        }

        Circle circle;


        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.w("MyRender", "onSurfaceCreated");
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            circle = new Circle(context);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.w("MyRender", "onSurfaceChanged");
            GLES20.glViewport(0, 0, width, height);
            //设置投影矩阵
            circle.projectionMatrix(width, height);
        }

        public void onDrawFrame(GL10 gl) {
            Log.w("MyRender", "onDrawFrame");
            GLES20.glClear(GL_COLOR_BUFFER_BIT);
            circle.draw();
        }
    }


        // 画圆圈
        public class MyRenderer5 implements GLSurfaceView.Renderer {
            // 定义圆心坐标
            private float x = 0.0f;
            private float y = 0.0f;
            // 半径
            private float r = 0.6f;
            // 三角形分割的数量
            private int count = 30;

            private float[] mColor = new float[]{   // RGBA   // 这里我用的是红色
                    1, 0, 0, 1,
                    1, 0, 0, 1,
                    1, 0, 0, 1
            };

            FloatBuffer circleFloatBuffer;
            FloatBuffer colorFloatBuffer;
            private float mDegree = 0.0f;
            private Points eye = new Points(0f, 0f, -3f);
            private Points up = new Points(0f, 1f, 0f);
            private Points center = new Points(0f, 0f, 0f);

            class Points {
                private float x, y, z;

                public Points(float x, float y, float z) {
                    this.x = x;
                    this.y = y;
                    this.z = z;
                }
            }

            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                Log.w("xulei", "MyRenderer5 onSurfaceCreated");
                gl.glShadeModel(GL10.GL_SMOOTH);
                // 黑色背景
                gl.glClearColor(0, 0, 0, 0);

                final int nodeCount = count + 1;
                float circleCoords[] = new float[nodeCount * 3]; // x,y,z 三个坐标
                float colorCoords[] = new float[nodeCount * 4]; // RGBA 4个坐标
                // x y z
                int offset = 0;
                int offset2 = 0;
//                circleCoords[offset++] = 0;// 中心点
//                circleCoords[offset++] = 0;
//                circleCoords[offset++] = 0;
                for (int i = 0; i < count + 1; i++) {
                    float angleInRadians = ((float) i / (float) count) * ((float) Math.PI * 2f);
                    circleCoords[offset++] = x + r * (float) Math.sin(angleInRadians);
                    circleCoords[offset++] = y + r * (float) Math.cos(angleInRadians);
                    circleCoords[offset++] = 0;

                    colorCoords[offset2++] = ((float) i / (float) nodeCount);
                    colorCoords[offset2++] = 0.5f;
                    colorCoords[offset2++] = 0.3f;
                    colorCoords[offset2++] = 1.0f;

                }

                circleFloatBuffer = OpenGlesUtils.floatToBuffer(circleCoords);
                colorFloatBuffer = OpenGlesUtils.floatToBuffer(colorCoords);

            }

            public void onSurfaceChanged(GL10 gl, int width, int height) {
                Log.w("xulei", "MyRenderer5  onSurfaceChanged");
                float ratio = (float) width / height;
                //设置OpenGL场景的大小
                gl.glViewport(0, 0, width, height);
                //设置投影矩阵
                gl.glMatrixMode(GL10.GL_PROJECTION);
                //重置投影矩阵
                gl.glLoadIdentity();
                // 设置视口的大小
                gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
                // 选择模型观察矩阵
                gl.glMatrixMode(GL10.GL_MODELVIEW);
                // 重置模型观察矩阵
                gl.glLoadIdentity();
            }



            public void onDrawFrame(GL10 gl) {
                Log.w("xulei", "MyRenderer5  onDrawFrame");
                // 清除屏幕和深度缓存
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                // 重置当前的模型观察矩阵
                gl.glLoadIdentity();

                // 允许设置顶点
                //GL10.GL_VERTEX_ARRAY顶点数组
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                // 允许设置颜色
                //GL10.GL_COLOR_ARRAY颜色数组
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

//                gl.glRotatef(0, 0, 1, 0);
                //将三角形在z轴上移动
//                gl.glTranslatef(0f, 0.0f, -1.0001f);


                //眼睛对着原点看
                GLU.gluLookAt(gl, eye.x, eye.y, eye.z, center.x,
                        center.y, center.z, up.x, up.y, up.z);

                //为了能有立体感觉，通过改变mDegree值，让模型不断旋转
                gl.glRotatef(mDegree, 0, 1, 0);

                //将模型放缩到View刚好装下
//                gl.glScalef(mScalef, mScalef, mScalef);
                //把模型移动到原点


                // 设置三角形
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, circleFloatBuffer);
                // 设置三角形颜色
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorFloatBuffer);
                // 绘制三角形
                gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, count+1);


                // 取消颜色设置
                gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
                // 取消顶点设置
                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

                //绘制结束
                gl.glFinish();

            }

            public void rotate(float degree) {
                mDegree = degree;
            }

        }


}
