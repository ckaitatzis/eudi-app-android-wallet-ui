/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package eu.europa.ec.dashboardfeature.ui.sidemenu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.europa.ec.dashboardfeature.model.SideMenuItemType
import eu.europa.ec.dashboardfeature.model.SideMenuItemUi
import eu.europa.ec.dashboardfeature.ui.dashboard.Event
import eu.europa.ec.dashboardfeature.ui.dashboard.State
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.ListItemData
import eu.europa.ec.uilogic.component.ListItemLeadingContentData
import eu.europa.ec.uilogic.component.ListItemMainContentData
import eu.europa.ec.uilogic.component.ListItemTrailingContentData
import eu.europa.ec.uilogic.component.content.ContentScreen
import eu.europa.ec.uilogic.component.content.ContentTitle
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.uilogic.component.preview.PreviewTheme
import eu.europa.ec.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.uilogic.component.utils.SPACING_SMALL
import eu.europa.ec.uilogic.component.wrap.WrapListItem

@Composable
internal fun SideMenuScreen(
    state: State,
    onEventSent: (Event) -> Unit,
) {
    ContentScreen(
        navigatableAction = ScreenNavigateAction.BACKABLE,
        isLoading = false,
        onBack = {
            onEventSent(
                Event.SideMenu.Hide
            )
        }
    ) { paddingValues ->
        Content(
            state = state,
            paddingValues = paddingValues,
            onEventSent = onEventSent
        )
    }
}

@Composable
private fun Content(
    state: State,
    onEventSent: (Event) -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            ContentTitle(
                modifier = Modifier.fillMaxWidth(),
                title = state.sideMenuTitle,
            )

            SideMenuOptions(
                sideMenuOptions = state.sideMenuOptions,
                onEventSent = onEventSent,
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SPACING_MEDIUM.dp),
            text = state.appVersion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SideMenuOptions(
    sideMenuOptions: List<SideMenuItemUi>,
    onEventSent: (Event) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(sideMenuOptions) { index, menuOption ->
            WrapListItem(
                modifier = Modifier.fillMaxWidth(),
                mainContentVerticalPadding = SPACING_MEDIUM.dp,
                item = menuOption.data,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                onItemClick = {
                    onEventSent(
                        Event.SideMenu.ItemClicked(itemType = menuOption.type)
                    )
                }
            )

            if (index != sideMenuOptions.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SPACING_SMALL.dp)
                )
            }
        }
    }
}

@ThemeModePreviews
@Composable
private fun SideMenuContentPreview() {
    PreviewTheme {
        Content(
            state = State(
                isSideMenuVisible = true,
                sideMenuTitle = stringResource(R.string.dashboard_side_menu_title),
                sideMenuOptions = listOf(
                    SideMenuItemUi(
                        type = SideMenuItemType.CHANGE_PIN,
                        data = ListItemData(
                            itemId = "changePinId",
                            mainContentData = ListItemMainContentData.Text(
                                text = stringResource(R.string.dashboard_side_menu_change_pin)
                            ),
                            leadingContentData = ListItemLeadingContentData.Icon(
                                iconData = AppIcons.ChangePin
                            ),
                            trailingContentData = ListItemTrailingContentData.Icon(
                                iconData = AppIcons.KeyboardArrowRight
                            )
                        )
                    ),
                    SideMenuItemUi(
                        type = SideMenuItemType.CHANGE_PIN,
                        data = ListItemData(
                            itemId = "changePinId",
                            mainContentData = ListItemMainContentData.Text(
                                text = stringResource(R.string.dashboard_side_menu_change_pin)
                            ),
                            leadingContentData = ListItemLeadingContentData.Icon(
                                iconData = AppIcons.ChangePin
                            ),
                            trailingContentData = ListItemTrailingContentData.Icon(
                                iconData = AppIcons.KeyboardArrowRight
                            )
                        )
                    ),
                    SideMenuItemUi(
                        type = SideMenuItemType.CHANGE_PIN,
                        data = ListItemData(
                            itemId = "changePinId",
                            mainContentData = ListItemMainContentData.Text(
                                text = stringResource(R.string.dashboard_side_menu_change_pin)
                            ),
                            leadingContentData = ListItemLeadingContentData.Icon(
                                iconData = AppIcons.ChangePin
                            ),
                            trailingContentData = ListItemTrailingContentData.Icon(
                                iconData = AppIcons.KeyboardArrowRight
                            )
                        )
                    ),
                ),
                appVersion = "1.0.0"
            ),
            onEventSent = {},
            paddingValues = PaddingValues(SPACING_MEDIUM.dp)
        )
    }
}