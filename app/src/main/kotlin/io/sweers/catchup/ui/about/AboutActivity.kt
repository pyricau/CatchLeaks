/*
 * Copyright (C) 2019. Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sweers.catchup.ui.about

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.text.style.StyleSpan
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.layoutDirection
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.uber.autodispose.autoDispose
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.noties.markwon.Markwon
import io.sweers.catchup.R
import io.sweers.catchup.base.ui.InjectingBaseActivity
import io.sweers.catchup.base.ui.InjectingBaseFragment
import io.sweers.catchup.base.ui.VersionInfo
import io.sweers.catchup.data.LinkManager
import io.sweers.catchup.databinding.ActivityGenericContainerBinding
import io.sweers.catchup.databinding.FragmentAboutBinding
import io.sweers.catchup.databinding.FragmentLearnMoreBinding
import io.sweers.catchup.injection.ActivityModule
import io.sweers.catchup.injection.scopes.PerFragment
import io.sweers.catchup.service.api.UrlMeta
import io.sweers.catchup.ui.Scrollable
import io.sweers.catchup.util.LinkTouchMovementMethod
import io.sweers.catchup.util.TouchableUrlSpan
import io.sweers.catchup.util.UiUtil
import io.sweers.catchup.util.buildMarkdown
import io.sweers.catchup.util.customtabs.CustomTabActivityHelper
import io.sweers.catchup.util.isInNightMode
import io.sweers.catchup.util.kotlin.windowed
import io.sweers.catchup.util.parseMarkdownAndPlainLinks
import io.sweers.catchup.util.setLightStatusBar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.ldralighieri.corbind.material.offsetChanges
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class AboutActivity : InjectingBaseActivity() {

  @Inject
  internal lateinit var customTab: CustomTabActivityHelper

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycle()
        .doOnStart(customTab) { bindCustomTabsService(this@AboutActivity) }
        .doOnStop(customTab) { unbindCustomTabsService(this@AboutActivity) }
        .doOnDestroy(customTab) { connectionCallback = null }
        .autoDispose(this)
        .subscribe()

    val viewGroup = viewContainer.forActivity(this)
    ActivityGenericContainerBinding.inflate(layoutInflater, viewGroup, true)

    if (savedInstanceState == null) {
      supportFragmentManager.commitNow {
        add(R.id.fragment_container, AboutFragment())
      }
    }
  }

  @dagger.Module
  abstract class Module : ActivityModule<AboutActivity>
}

class LearnMoreFragment : InjectingBaseFragment<FragmentLearnMoreBinding>() {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLearnMoreBinding =
    FragmentLearnMoreBinding::inflate
}

class AboutFragment : InjectingBaseFragment<FragmentAboutBinding>() {

  companion object {
    private const val FADE_PERCENT = 0.75F
    private const val TITLE_TRANSLATION_PERCENT = 0.50F
  }

  @Inject
  internal lateinit var linkManager: LinkManager
  @Inject
  internal lateinit var markwon: Markwon
  @Inject
  internal lateinit var versionInfo: VersionInfo

  private val bannerContainer get() = binding.bannerContainer
  private val bannerIcon get() = binding.bannerIcon
  private val aboutText get() = binding.bannerText
  private val title get() = binding.bannerTitle
  private val learnMore get() = binding.buttonLearnMore


  private lateinit var compositeClickSpan: (String) -> Set<Any>

  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAboutBinding =
      FragmentAboutBinding::inflate

  @SuppressLint("SetTextI18n")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    compositeClickSpan = { url: String ->
      setOf(
          object : TouchableUrlSpan(url, aboutText.linkTextColors, 0) {
            override fun onClick(url: String) {
              viewLifecycleOwner.lifecycleScope.launch {
                linkManager.openUrl(
                    UrlMeta(url, aboutText.highlightColor,
                        activity!!))
              }
            }
          },
          StyleSpan(Typeface.BOLD)
      )
    }

    bannerIcon.setOnLongClickListener {
      Toast.makeText(activity, R.string.icon_attribution, Toast.LENGTH_SHORT).show()
      viewLifecycleOwner.lifecycleScope.launch {
        linkManager.openUrl(
            UrlMeta("https://cookicons.co", aboutText.highlightColor, activity!!))
      }
      true
    }

    aboutText.movementMethod = LinkTouchMovementMethod.getInstance()
    aboutText.text = buildMarkdown {
      text(aboutText.resources.getString(R.string.about_description))
      newline(3)
      text(aboutText.resources.getString(R.string.about_version, versionInfo.name))
      newline(2)
      text(aboutText.resources.getString(R.string.about_by))
      space()
      link("https://twitter.com/ZacSweers", "Zac Sweers")
      text(" - ")
      link("https://github.com/ZacSweers/CatchUp",
          aboutText.resources.getString(R.string.about_source_code))
    }.parseMarkdownAndPlainLinks(
        on = aboutText,
        with = markwon,
        alternateSpans = compositeClickSpan)

    learnMore.setOnClickListener {
      requireFragmentManager().commit {
        replace(R.id.fragment_container, LearnMoreFragment())
        addToBackStack(null)
        setCustomAnimations(R.anim.slide_up, R.anim.inset)
      }
    }
  }
 }

@Module
abstract class AboutFragmentBindingModule {

  @PerFragment
  @ContributesAndroidInjector
  internal abstract fun aboutFragment(): AboutFragment

  @PerFragment
  @ContributesAndroidInjector
  internal abstract fun learnMoreFragment(): LearnMoreFragment

  @PerFragment
  @ContributesAndroidInjector
  internal abstract fun licensesFragment(): LicensesFragment

  @PerFragment
  @ContributesAndroidInjector
  internal abstract fun changelogFragment(): ChangelogFragment
}

private enum class ScrollDirection {
  UP, DOWN;

  companion object {
    fun resolve(current: Int, prev: Int): ScrollDirection {
      return if (current > prev) {
        DOWN
      } else {
        UP
      }
    }
  }
}
