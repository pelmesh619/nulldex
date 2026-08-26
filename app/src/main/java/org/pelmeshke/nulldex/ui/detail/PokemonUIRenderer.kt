package org.pelmeshke.nulldex.ui.detail

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import org.pelmeshke.nulldex.R
import org.pelmeshke.nulldex.data.model.PokemonUIComponent
import org.pelmeshke.nulldex.data.model.PokemonUIModel
import org.pelmeshke.nulldex.ui.view.PokemonTypeView

class PokemonUIRenderer(
    private val container: LinearLayout,
    private val actionHandler: PokemonActionHandler,
    private val analyticsTracker: AnalyticsTracker
) {
    private val context = container.context
    private val inflater = LayoutInflater.from(context)
    private var appearIndex = 0

    fun render(model: PokemonUIModel) {
        container.removeAllViews()
        appearIndex = 0

        val landscape = context.resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        container.orientation = if (landscape) {
            LinearLayout.HORIZONTAL
        } else {
            LinearLayout.VERTICAL
        }
        container.setBackgroundColor(model.primaryColor)

        val hero = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            background = heroGradient(model.primaryColor)
        }
        val sheet = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(
                context,
                if (landscape) R.drawable.bg_sdui_sheet_land else R.drawable.bg_sdui_sheet
            )
            val pad = resources.getDimensionPixelSize(R.dimen.spacing_l)
            setPadding(pad, pad, pad, pad)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (landscape) {
                    width = 0
                    height = LinearLayout.LayoutParams.MATCH_PARENT
                    weight = 1f
                } else {
                    topMargin = -resources.getDimensionPixelSize(R.dimen.spacing_l)
                }
            }
        }

        val components = model.components
        var index = 0
        while (index < components.size) {
            val first = components[index]
            if (first is PokemonUIComponent.Stat) {
                val stats = mutableListOf<PokemonUIComponent.Stat>()
                while (index < components.size && components[index] is PokemonUIComponent.Stat) {
                    stats += components[index] as PokemonUIComponent.Stat
                    index++
                }
                val row = renderStatRow(stats)
                sheet.addView(row)
                appear(row)
                continue
            }

            val view = renderComponent(first, model.primaryColor)
            bindInteractions(view, first)
            if (isHero(first)) {
                hero.addView(view)
            } else {
                sheet.addView(view)
            }
            appear(view)
            index++
        }

        if (landscape) {
            hero.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            hero.gravity = android.view.Gravity.CENTER
            container.addView(scrollOf(hero, matchParent = true, weight = 1f))
            container.addView(scrollOf(sheet, matchParent = true, weight = 1f))
        } else {
            val column = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(hero)
                addView(renderComponent(PokemonUIComponent.Divider("hero_sheet_divider"), model.primaryColor))
                addView(sheet)
            }
            container.addView(scrollOf(column, matchParent = true, weight = 0f))
//            container.addView(hero)
//            container.addView(renderComponent(PokemonUIComponent.Divider("hero_sheet_divider"), model.primaryColor))
//            container.addView(sheet)
        }
    }

    private fun isHero(component: PokemonUIComponent): Boolean = when (component) {
        is PokemonUIComponent.Sprite,
        is PokemonUIComponent.Number,
        is PokemonUIComponent.Title,
        is PokemonUIComponent.TypeBadges -> true
        else -> false
    }

    private fun renderComponent(
        component: PokemonUIComponent,
        primaryColor: Int
    ): View = when (component) {
        is PokemonUIComponent.Sprite -> renderSprite(component)
        is PokemonUIComponent.Title -> renderTitle(component)
        is PokemonUIComponent.Number -> renderNumber(component)
        is PokemonUIComponent.TypeBadges -> renderTypeBadges(component)
        is PokemonUIComponent.Abilities -> renderAbilities(component)
        is PokemonUIComponent.Stat -> renderStat(component)
        is PokemonUIComponent.Divider -> inflater.inflate(R.layout.sdui_divider, container, false)
        is PokemonUIComponent.Section -> renderSection(component)
        is PokemonUIComponent.Button -> renderButton(component, primaryColor)
    }

    private fun bindInteractions(view: View, component: PokemonUIComponent) {
        component.analytics?.impressionEvent?.let { event ->
            analyticsTracker.track(
                event,
                component.analytics?.params.orEmpty() + mapOf("component_id" to component.id)
            )
        }

        if (component is PokemonUIComponent.Abilities) {
            return
        }

        val action = component.action
        if (action != null) {
            view.isClickable = true
            view.isFocusable = true
            view.setOnClickListener {
                trackClick(component)
                actionHandler.handle(component.id, action)
            }
        } else {
            view.setOnClickListener(null)
        }
    }

    private fun trackClick(component: PokemonUIComponent) {
        component.analytics?.clickEvent?.let { event ->
            analyticsTracker.track(
                event,
                component.analytics?.params.orEmpty() + mapOf("component_id" to component.id)
            )
        }
    }

    private fun renderSprite(component: PokemonUIComponent.Sprite): View {
        val view = inflater.inflate(R.layout.sdui_sprite, container, false)
        val image = view.findViewById<ImageView>(R.id.spriteImage)
        Glide.with(image)
            .load(component.url)
            .into(image)
        return view
    }

    private fun renderTitle(component: PokemonUIComponent.Title): View {
        val view = inflater.inflate(R.layout.sdui_title, container, false) as TextView
        view.text = component.text
        return view
    }

    private fun renderNumber(component: PokemonUIComponent.Number): View {
        val view = inflater.inflate(R.layout.sdui_number, container, false) as TextView
        view.text = component.text
        return view
    }

    private fun renderTypeBadges(component: PokemonUIComponent.TypeBadges): View {
        val row = inflater.inflate(R.layout.sdui_types, container, false) as LinearLayout
        val height = context.resources.getDimensionPixelSize(R.dimen.type_badge_height)
        val gap = context.resources.getDimensionPixelSize(R.dimen.spacing_s)
        component.types.forEachIndexed { index, type ->
                row.addView(PokemonTypeView(context).apply {
                typeName = type
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    height
                ).apply {
                    if (index > 0) marginStart = gap
                }
            })
        }
        return row
    }

    private fun renderSection(component: PokemonUIComponent.Section): View {
        val view = inflater.inflate(R.layout.sdui_section, container, false) as TextView
        view.text = component.title
        return view
    }

    private fun renderStatRow(stats: List<PokemonUIComponent.Stat>): View {
        val row = inflater.inflate(R.layout.sdui_stat_row, container, false) as LinearLayout
        val gap = context.resources.getDimensionPixelSize(R.dimen.spacing_s)
        stats.forEachIndexed { index, stat ->
            val card = renderStat(stat, row)
            card.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (index > 0) marginStart = gap
            }
            bindInteractions(card, stat)
            row.addView(card)
        }
        return row
    }

    private fun renderStat(component: PokemonUIComponent.Stat, parent: ViewGroup = container): View {
        val view = inflater.inflate(R.layout.sdui_stat, parent, false)
        view.findViewById<TextView>(R.id.statValue).text = component.value
        view.findViewById<TextView>(R.id.statLabel).text = component.label
        return view
    }

    private fun renderAbilities(component: PokemonUIComponent.Abilities): View {
        val view = inflater.inflate(R.layout.sdui_abilities, container, false)
        view.findViewById<TextView>(R.id.abilitiesTitle).text = component.title
        val toggle = view.findViewById<TextView>(R.id.abilitiesToggle)
        val list = view.findViewById<LinearLayout>(R.id.abilitiesList)
        val header = view.findViewById<View>(R.id.abilitiesHeader)

        component.abilities.forEach { ability ->
            val chip = inflater.inflate(R.layout.sdui_ability_chip, list, false) as TextView
            chip.text = ability
            list.addView(chip)
        }

        header.setOnClickListener {
            val expanded = list.visibility == View.VISIBLE
            list.visibility = if (expanded) View.GONE else View.VISIBLE
            toggle.setText(if (expanded) R.string.sdui_show else R.string.sdui_hide)
            trackClick(component)
            component.action?.let { actionHandler.handle(component.id, it) }
        }
        return view
    }

    private fun renderButton(component: PokemonUIComponent.Button, primaryColor: Int): View {
        val view = inflater.inflate(R.layout.sdui_button, container, false) as TextView
        view.text = component.label
        val background = ContextCompat.getDrawable(context, R.drawable.bg_sdui_button)
            ?.mutate() as? GradientDrawable
        background?.setColor(primaryColor)
        view.background = background
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = context.resources.getDimensionPixelSize(R.dimen.spacing_s)
        view.layoutParams = params
        return view
    }

    private fun heroGradient(primaryColor: Int): GradientDrawable {
        val top = ColorUtils.blendARGB(primaryColor, android.graphics.Color.BLACK, 0.18f)
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(top, primaryColor)
        )
    }

    private fun scrollOf(child: View, matchParent: Boolean, weight: Float): NestedScrollView {
        return NestedScrollView(context).apply {
            isFillViewport = true
            layoutParams = if (weight > 0f) {
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
            } else {
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
            val childParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (matchParent && weight > 0f) {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                }
            )
            addView(child, childParams)
        }
    }

    private fun appear(view: View) {
        val offset = context.resources.getDimensionPixelSize(R.dimen.spacing_m).toFloat()
        view.alpha = 0f
        view.translationY = offset
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(35L * appearIndex)
            .setDuration(320)
            .setInterpolator(DecelerateInterpolator())
            .start()
        appearIndex++
    }
}
