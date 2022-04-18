package xyz.keriteal.bili.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import xyz.keriteal.bili.databinding.CaptchaLayoutBinding

class CaptchaDialog : DialogFragment() {
    companion object {
        const val TAG = "CaptchaDialog"

        private const val KEY_VALIDATE = "KEY_VALIDATE"
        private const val KEY_SECCODE = "KEY_SECCODE"
        private const val KEY_GT = "KEY_GT"
        private const val KEY_CHALLENGE = "KEY_CHALLENGE"

        fun newInstance(gt: String, challenge: String): CaptchaDialog {
            val args = Bundle()
            args.putString(KEY_GT, gt)
            args.putString(KEY_CHALLENGE, challenge)
            val instance = CaptchaDialog()
            instance.arguments = args
            return instance
        }
    }

    private var _binding: CaptchaLayoutBinding? = null
    private val binding get() = _binding!!
    private var captchaListener: OnCaptchaListener? = null
    private lateinit var gt: String
    private lateinit var challenge: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = CaptchaLayoutBinding.inflate(inflater, container, false)
        gt = savedInstanceState!!.getString(KEY_GT, "")
        challenge = savedInstanceState!!.getString(KEY_CHALLENGE, "")
        val webView = binding.captchaWeb
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (view?.progress == 100) {
                    view.evaluateJavascript(
                        "javascript:loadCaptcha($gt, $challenge)",
                        ValueCallback { }
                    )
                }
            }
        }
        return binding.root
    }

    fun setOnCaptchaListener(listener: OnCaptchaListener) {
        this.captchaListener = listener
    }

    interface OnCaptchaListener {
        fun onCaptchaPassed(validate: String, seccode: String)
    }

    @JavascriptInterface
    fun captcha(validate: String, seccode: String) {
        captchaListener?.onCaptchaPassed(validate, seccode)
    }
}