package r12.seekbar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import r12.materialseekbar.MaterialSeekBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val seekBar = findViewById<MaterialSeekBar>(R.id.seekbar);
    }
}
