package lei.com.opengldemo;

import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by xulei on 18-7-4.
 */

public class Circle {


    private Context context;
    private FloatBuffer vertexData;
    // 定义圆心坐标
    private float x;
    private float y;
    // 半径
    private float r;
    // 三角形分割的数量
    private int count = 10;
    // 每个顶点包含的数据个数 （ x 和 y ）
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;

    private static final String U_COLOR = "u_Color";
    private static final String A_POSITION = "a_Position";
    private int mProgram;
    private int uColorLocation;
    private int aPositionLocation;
    private int mPositionHandle;

    /*
     * 第一步: 定义投影矩阵相关
     */
    private static final String U_MATRIX = "u_Matrix";
    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;

    public Circle(Context context) {
        this.context = context;
        x = 0f;
        y = 0f;
        r = 0.8f;
        initVertexData();
    }


    private void initVertexData() {
        // 顶点的个数，我们分割count个三角形，有count+1个点，再加上圆心共有count+2个点
        final int nodeCount = count + 2;
        float circleCoords[] = new float[nodeCount * POSITION_COMPONENT_COUNT];
        // x y
        int offset = 0;
        circleCoords[offset++] = x;// 中心点
        circleCoords[offset++] = y;
        for (int i = 0; i < count + 1; i++) {
            float angleInRadians = ((float) i / (float) count)
                    * ((float) Math.PI * 2f);
            circleCoords[offset++] = x + r * (float)Math.sin(angleInRadians);
            circleCoords[offset++] = y + r * (float)Math.cos(angleInRadians);
        }
        // 为存放形状的坐标，初始化顶点字节缓冲
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (坐标数 * 4)float占四字节
                circleCoords.length * BYTES_PER_FLOAT);
        // 设用设备的本点字节序
        bb.order(ByteOrder.nativeOrder());
        // 从ByteBuffer创建一个浮点缓冲
        vertexData = bb.asFloatBuffer();
        // 把坐标们加入FloatBuffer中
        vertexData.put(circleCoords);
        // 设置buffer，从第一个坐标开始读
        vertexData.position(0);
//        vertexData = OpenGlesUtils.floatToBuffer(circleCoords);

//        getProgram();


        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        mProgram = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        GLES20.glUseProgram(mProgram);

//        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
//
//        GLES20.glEnableVertexAttribArray(mPositionHandle);
//        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
//                12, mVertexBuffer);

        uColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, "vPosition");


		/*
		 * 第二步: 获取顶点着色器中投影矩阵的location
		 */
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);

        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                GLES20.GL_FLOAT, false, 0, vertexData);

        GLES20.glEnableVertexAttribArray(aPositionLocation);
    }

    private static final String VERTEX_SHADER =
            "attribute vec4 vPosition;\n"
                    + "void main() {\n"
                    + " gl_Position = vPosition;\n"
                    + "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "void main() {\n"
                    + " gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                    + "}";

    private void getProgram(){
//        String vertexShaderSource = TextResourceReader
//                .readTextFileFromResource(context, R.raw.vertex_shader);
//        String fragmentShaderSource = TextResourceReader
//                .readTextFileFromResource(context, R.raw.simple_fragment_shader);
//
//        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
//        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, OpenGlesUtils.DRAW_VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, OpenGlesUtils.DRAW_FRAGMENT_SHADER);

        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

//        if (LoggerConfig.ON) {
//            ShaderHelper.validateProgram(program);
//        }

        GLES20.glUseProgram(mProgram);
    }
    /**
     * 第三步 ： 根据屏幕的width 和 height 创建投影矩阵
     * @param width
     * @param height
     */
    public void projectionMatrix(int width,int height){
        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if(width > height){
            Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        }else{
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
    }

    public void draw(){
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
		/*
		 * 第四步：传入投影矩阵
		 */
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, count +2);
    }

    static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

}
