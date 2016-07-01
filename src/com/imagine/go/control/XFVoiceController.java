package com.imagine.go.control;

import static com.imagine.go.Constants.ID_APP_XFVoice;
import static com.imagine.go.Constants.IS_DEBUG;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

/**
 * XFVoiceController: 讯飞语音组件
 * 
 * @语音听写
 * @语音播报
 * @author Jinhu
 * @date 2016/3/29
 */
public class XFVoiceController {
	private static final String TAG = XFVoiceController.class.getSimpleName();

	private static Context mContext;

	/* 声音合成组件. */
	private final TTSUnit mTtsUnit;

	/* 语音听写组件. */
	private final STTUnit mSttUnit;

	/* 语音识别结束回调. */
	private OnSpeechEndedListener mOnSpeechEndedListener;

	public interface OnSpeechEndedListener {
		void onResult(String result);
	}

	public XFVoiceController(Context context) {
		mContext = context;
		// 注册AppID
		SpeechUtility.createUtility(context, SpeechConstant.APPID + "="
				+ ID_APP_XFVoice);
		mTtsUnit = new TTSUnit();
		mSttUnit = new STTUnit();

	}

	/**
	 * 听写文字
	 */
	public void startListening() {
		mSttUnit.startListening();
	}

	/**
	 * 语音对话框听写文字
	 */
	public void startListeningByDialog(Context context) {
		RecognizerDialog mDialog = new RecognizerDialog(context, null);
		// 参数设置
		mDialog.setParameter(SpeechConstant.DOMAIN, mSttUnit.DOMAIN);
		mDialog.setParameter(SpeechConstant.LANGUAGE, mSttUnit.LANGUAGE);
		mDialog.setParameter(SpeechConstant.ACCENT, mSttUnit.ACCENT);
		mDialog.setParameter(SpeechConstant.VAD_BOS, mSttUnit.VAD_BOS);
		mDialog.setParameter(SpeechConstant.VAD_EOS, mSttUnit.VAD_EOS);
		mDialog.setParameter(SpeechConstant.ASR_PTT, mSttUnit.ASR_PTT);
		// 设置监听
		mDialog.setListener(mSttUnit);

		mDialog.show();
	}

	/**
	 * 播报文字
	 * 
	 * @param text
	 */
	public void startSpeaking(String text) {

		// 开始播放
		mTtsUnit.startSpeaking(text);

		if (IS_DEBUG) {
			Log.d(TAG, "--TTSUnit speakText()--");
		}
	}

	/**
	 * 停止播报文字
	 */
	public void stopSeaking() {
		// 停止之前的播放
		mTtsUnit.stopSeaking();
		if (IS_DEBUG) {
			Log.d(TAG, "--TTSUnit stopSpeakText()--");
		}
	}

	/**
	 * 销毁
	 */
	public void onDestroy() {
		mTtsUnit.destory();
		mSttUnit.destroy();
	}

	/**
	 * 解析语音听写返回的json结果
	 * 
	 * @param json
	 * @return
	 */
	private String parseSttResult(String json) {
		StringBuffer ret = new StringBuffer();
		try {
			JSONTokener tokener = new JSONTokener(json);
			JSONObject joResult = new JSONObject(tokener);

			JSONArray words = joResult.getJSONArray("ws");
			for (int i = 0; i < words.length(); i++) {
				// 转写结果词，默认使用第一个结果
				JSONArray items = words.getJSONObject(i).getJSONArray("cw");
				JSONObject obj = items.getJSONObject(0);
				ret.append(obj.getString("w"));
				// 如果需要多候选结果，解析数组其他字段
				// for(int j = 0; j < items.length(); j++)
				// {
				// JSONObject obj = items.getJSONObject(j);
				// ret.append(obj.getString("w"));
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return ret.toString();
	}

	public void setOnSpeechEndedListener(
			OnSpeechEndedListener onSpeechEndedListener) {
		mOnSpeechEndedListener = onSpeechEndedListener;
	}

	/**
	 * STTUnit
	 * 
	 * @听写器:声音转化文字组件
	 * @author Jinhu
	 * @date 2016/3/29
	 */
	private class STTUnit implements RecognizerListener,
			RecognizerDialogListener {

		/* 声音识别 */
		private SpeechRecognizer mStt;

		/* 应用领域:短信和日常用语 */
		private final String DOMAIN = "iat";
		/* 语言:中文 */
		private final String LANGUAGE = "zh_cn";
		/* 方言:普通话 */
		private final String ACCENT = "mandarin";
		/* 静音超时时间 */
		private final String VAD_BOS = "5000";
		/* 静音检测 即停止录音时间 */
		private final String VAD_EOS = "2000";
		/* 无标点符号 */
		private final String ASR_PTT = "0";

		/* 保存录音结果 */
		private Map<String, String> mResults = new HashMap<String, String>();

		public STTUnit() {
			initSetting();
		}

		/**
		 * 开始听写
		 */
		public void startListening() {
			mStt.startListening(this);
		}

		/**
		 * 停止听写
		 */
		public void stopListening() {
			if (mStt.isListening()) {
				mStt.stopListening();
			}
		}

		public void destroy() {
			mResults.clear();
			mStt.cancel();
			mStt.destroy();
		}

		/**
		 * 初始化配置
		 */
		private void initSetting() {
			// 创建SpeechRecognizer对象
			mStt = SpeechRecognizer.createRecognizer(mContext, null);
			// 参数设置
			mStt.setParameter(SpeechConstant.DOMAIN, DOMAIN);
			mStt.setParameter(SpeechConstant.LANGUAGE, LANGUAGE);
			mStt.setParameter(SpeechConstant.ACCENT, ACCENT);
			mStt.setParameter(SpeechConstant.VAD_BOS, VAD_BOS);
			mStt.setParameter(SpeechConstant.VAD_EOS, VAD_EOS);
			mStt.setParameter(SpeechConstant.ASR_PTT, ASR_PTT);
		}

		/**
		 * 解析回调结果
		 * 
		 * @param results
		 */
		private void printResult(RecognizerResult results) {
			String text = parseSttResult(results.getResultString());
			String sn = null;
			// 读取json结果中的sn字段
			try {
				JSONObject resultJson = new JSONObject(
						results.getResultString());
				sn = resultJson.optString("sn");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			mResults.put(sn, text);

		}

		// ----------------语音听写回调------------------
		// 听写结果回调
		@Override
		public void onResult(RecognizerResult result, boolean islast) {
			printResult(result);
			if (islast) {
				if (null == mOnSpeechEndedListener)
					return;
				StringBuffer resultBuffer = new StringBuffer();
				for (String key : mResults.keySet()) {
					resultBuffer.append(mResults.get(key));
				}
				mOnSpeechEndedListener.onResult(resultBuffer.toString());
			}
		}

		// 开始录音
		@Override
		public void onBeginOfSpeech() {

		}

		// 结束录音
		@Override
		public void onEndOfSpeech() {
		}

		// 会话发生错误回调
		@Override
		public void onError(SpeechError error) {
			// 获取错误码描述
			error.getPlainDescription(true);
		}

		// 扩展用接口
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

		}

		// 音量改变
		@Override
		public void onVolumeChanged(int arg0, byte[] arg1) {

		}
	}

	/**
	 * TTSUnit
	 * 
	 * @声音合成器:文字转化声音组件
	 * @author Jinhu
	 * @date 2016/3/29
	 */
	private class TTSUnit implements SynthesizerListener {

		/* 声音合成器 */
		private SpeechSynthesizer mTts;
		/* 标记播放状态 */
		private boolean isFinish;

		/* 发音人 */
		private final String VOICE_NAME = "xiaoyan";
		/* 语速 */
		private final String SPEED = "50";
		/* 音量 */
		private final String VOLUME = "80";
		/* 在线模式 */
		private final String ENGINT_TYPE = SpeechConstant.TYPE_CLOUD;

		public TTSUnit() {
			initSetting();
		}

		/**
		 * 初始化配置
		 */
		private void initSetting() {
			// 创建SpeechSynthesizer对象
			mTts = SpeechSynthesizer.createSynthesizer(mContext, null);
			// 合成参数设置
			mTts.setParameter(SpeechConstant.VOICE_NAME, VOICE_NAME);
			mTts.setParameter(SpeechConstant.SPEED, SPEED);
			mTts.setParameter(SpeechConstant.VOLUME, VOLUME);
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, ENGINT_TYPE);

			isFinish = true;
		}

		/**
		 * 开始播放
		 * 
		 * @param text
		 */
		public void startSpeaking(String text) {
			if (!isFinish)
				return;
			mTts.startSpeaking(text, TTSUnit.this);
		}

		/**
		 * 停止播放
		 */
		public void stopSeaking() {
			if (mTts.isSpeaking()) {
				mTts.stopSpeaking();
			}
			isFinish = true;
		}

		/**
		 * 销毁
		 */
		public void destory() {
			mTts.destroy();
		}

		// ----------------声音合成器回调------------------
		// 播放结束
		@Override
		public void onCompleted(SpeechError paramSpeechError) {
			isFinish = true;
		}

		// 开始播放
		@Override
		public void onSpeakBegin() {
			isFinish = false;
		}

		// 缓冲进度回调
		@Override
		public void onBufferProgress(int paramInt1, int paramInt2,
				int paramInt3, String paramString) {

		}

		// 暂停播放回调
		@Override
		public void onSpeakPaused() {
		}

		// 恢复播放回调
		@Override
		public void onSpeakResumed() {
		}

		// 播放进度回调
		@Override
		public void onSpeakProgress(int paramInt1, int paramInt2, int paramInt3) {
		}

		// 会话事件回调
		@Override
		public void onEvent(int paramInt1, int paramInt2, int paramInt3,
				Bundle paramBundle) {
		}

	}

}
