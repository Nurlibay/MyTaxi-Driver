package uz.nurlibaydev.mytaxitesttask.presetation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import uz.nurlibaydev.mytaxitesttask.R
import uz.nurlibaydev.mytaxitesttask.databinding.ScreenMainBinding

/**
 *  Created by Nurlibay Koshkinbaev on 08/03/2023 17:07
 */

class MainScreen : Fragment(R.layout.screen_main) {

    private val binding by viewBinding<ScreenMainBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

        }
    }
}