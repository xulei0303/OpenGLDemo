package lei.com.opengldemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

public class MainActivity extends Activity {

    private boolean supportsEs2;
    private GLSurfaceView glView;

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

        glView.setRenderer(new GLRenderer2());
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
        }
    }

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

}
