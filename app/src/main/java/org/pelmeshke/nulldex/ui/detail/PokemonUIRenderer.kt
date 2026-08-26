package org.pelmeshke.nulldex.ui.detail

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import org.pelmeshke.nulldex.R
import org.pelmeshke.nulldex.data.model.PokemonUIComponent
import org.pelmeshke.nulldex.data.model.PokemonUIModel
import org.pelmeshke.nulldex.ui.view.PokemonTypeView
import androidx.core.graphics.toColorInt

class PokemonUIRenderer(
    private val container: LinearLayout,
    private val actionHandler: PokemonActionHandler,
    private val analyticsTracker: AnalyticsTracker
) {

    fun render(model: PokemonUIModel) {
        container.removeAllViews()
        container.setBackgroundColor(model.primaryColor)

        model.components.forEach { component ->
            val view = when (component) {
                is PokemonUIComponent.Sprite   -> renderSprite(component)
                is PokemonUIComponent.Title    -> renderTitle(component)
                is PokemonUIComponent.Number   -> renderNumber(component)
                is PokemonUIComponent.TypeBadges -> renderTypeBadges(component)
                is PokemonUIComponent.Abilities -> renderAbilities(component)
                is PokemonUIComponent.Stat     -> renderStat(component)
                is PokemonUIComponent.Divider  -> renderDivider()
            }
            bindInteractions(view, component)
            container.addView(view)
        }
    }

    private fun bindInteractions(view: View, component: PokemonUIComponent) {
        val analytics = component.analyticsOrNull()
        analytics?.impressionEvent?.let { event ->
            analyticsTracker.track(
                event,
                analytics.params + mapOf("component_id" to component.componentId())
            )
        }

        val action = component.actionOrNull()
        if (action != null) {
            view.isClickable = true
            view.isFocusable = true
            view.setOnClickListener {
                analytics?.clickEvent?.let { event ->
                    analyticsTracker.track(
                        event,
                        analytics.params + mapOf("component_id" to component.componentId())
                    )
                }
                actionHandler.handle(component.componentId(), action)
            }
        } else {
            view.setOnClickListener(null)
        }
    }

    private fun renderSprite(component: PokemonUIComponent.Sprite): View {
        return ImageView(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                container.context.resources.getDimensionPixelSize(R.dimen.sprite_detail),
                container.context.resources.getDimensionPixelSize(R.dimen.sprite_detail)
            ).also { it.gravity = Gravity.CENTER_HORIZONTAL }
            Glide.with(this).load(component.url).into(this)
        }
    }

    private fun renderTitle(component: PokemonUIComponent.Title): View {
        return TextView(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = component.text
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            val p = container.context.resources.getDimensionPixelSize(R.dimen.spacing_s)
            setPadding(p, p, p, p)
        }
    }

    private fun renderNumber(component: PokemonUIComponent.Number): View {
        return TextView(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = component.text
            textSize = 14f
            setTextColor("#CCFFFFFF".toColorInt())
            gravity = Gravity.CENTER
        }
    }

    private fun renderTypeBadges(component: PokemonUIComponent.TypeBadges): View {
        return LinearLayout(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.gravity = Gravity.CENTER_HORIZONTAL }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            component.types.forEach { type ->
                addView(PokemonTypeView(context).apply {
                    typeName = type
                    layoutParams = LinearLayout.LayoutParams(200, 60).apply {
                        marginEnd = 8
                    }
                })
            }
        }
    }

    private fun renderStat(component: PokemonUIComponent.Stat): View {
        return LinearLayout(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            val p = container.context.resources.getDimensionPixelSize(R.dimen.spacing_m)
            setPadding(p, p / 2, p, p / 2)

            addView(TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = component.label
                textSize = 16f
                setTextColor(Color.WHITE)
            })
            addView(TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = component.value
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.WHITE)
            })
        }
    }

    private fun renderDivider(): View {
        return View(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).also {
                val m = container.context.resources.getDimensionPixelSize(R.dimen.spacing_m)
                it.setMargins(m, m / 2, m, m / 2)
            }
            setBackgroundColor("#33FFFFFF".toColorInt())
        }
    }

    private fun renderAbilities(component: PokemonUIComponent.Abilities): View {
        val horizontalPadding = container.context.resources.getDimensionPixelSize(R.dimen.spacing_m)
        val verticalPadding = container.context.resources.getDimensionPixelSize(R.dimen.spacing_s)
        val itemSpacing = container.context.resources.getDimensionPixelSize(R.dimen.spacing_xs)

        return LinearLayout(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

            val listContainer = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
            }

            val toggleLabel = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "Show"
                textSize = 14f
                setTextColor("#CCFFFFFF".toColorInt())
            }

            addView(LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    val isExpanded = listContainer.visibility == View.VISIBLE
                    listContainer.visibility = if (isExpanded) View.GONE else View.VISIBLE
                    toggleLabel.text = if (isExpanded) "Show" else "Hide"
                }

                addView(TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    text = component.title
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(Color.WHITE)
                })

                addView(toggleLabel)
            })

            component.abilities.forEachIndexed { index, ability ->
                listContainer.addView(TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index == 0) {
                            topMargin = container.context.resources.getDimensionPixelSize(R.dimen.spacing_s)
                        } else {
                            topMargin = itemSpacing
                        }
                    }
                    text = "\u2022 $ability"
                    textSize = 15f
                    setTextColor("#F2FFFFFF".toColorInt())
                })
            }

            addView(listContainer)
        }
    }

    private fun PokemonUIComponent.componentId(): String = when (this) {
        is PokemonUIComponent.Sprite -> id
        is PokemonUIComponent.Title -> id
        is PokemonUIComponent.Number -> id
        is PokemonUIComponent.TypeBadges -> id
        is PokemonUIComponent.Abilities -> id
        is PokemonUIComponent.Stat -> id
        is PokemonUIComponent.Divider -> id
    }

    private fun PokemonUIComponent.actionOrNull() = when (this) {
        is PokemonUIComponent.Sprite -> action
        is PokemonUIComponent.Title -> action
        is PokemonUIComponent.Number -> action
        is PokemonUIComponent.TypeBadges -> action
        is PokemonUIComponent.Abilities -> action
        is PokemonUIComponent.Stat -> action
        is PokemonUIComponent.Divider -> null
    }

    private fun PokemonUIComponent.analyticsOrNull() = when (this) {
        is PokemonUIComponent.Sprite -> analytics
        is PokemonUIComponent.Title -> analytics
        is PokemonUIComponent.Number -> analytics
        is PokemonUIComponent.TypeBadges -> analytics
        is PokemonUIComponent.Abilities -> analytics
        is PokemonUIComponent.Stat -> analytics
        is PokemonUIComponent.Divider -> analytics
    }
}
