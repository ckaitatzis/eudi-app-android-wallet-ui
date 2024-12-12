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

package eu.europa.ec.issuancefeature.interactor.document

import android.content.Context
import eu.europa.ec.authenticationlogic.controller.authentication.BiometricsAvailability
import eu.europa.ec.authenticationlogic.controller.authentication.DeviceAuthenticationResult
import eu.europa.ec.authenticationlogic.model.BiometricCrypto
import eu.europa.ec.businesslogic.extension.safeAsync
import eu.europa.ec.commonfeature.config.IssuanceFlowUiConfig
import eu.europa.ec.commonfeature.config.SuccessUIConfig
import eu.europa.ec.commonfeature.interactor.DeviceAuthenticationInteractor
import eu.europa.ec.commonfeature.model.DocumentOptionItemUi
import eu.europa.ec.commonfeature.model.toUiName
import eu.europa.ec.corelogic.controller.AddSampleDataPartialState
import eu.europa.ec.corelogic.controller.IssuanceMethod
import eu.europa.ec.corelogic.controller.IssueDocumentPartialState
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.model.DocType
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.issuancefeature.ui.document.add.Event
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.resourceslogic.theme.values.ThemeColors
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.ListItemData
import eu.europa.ec.uilogic.component.ListItemTrailingContentData
import eu.europa.ec.uilogic.component.wrap.CheckboxData
import eu.europa.ec.uilogic.component.wrap.ExpandableListItemData
import eu.europa.ec.uilogic.config.ConfigNavigation
import eu.europa.ec.uilogic.config.NavigationType
import eu.europa.ec.uilogic.navigation.CommonScreens
import eu.europa.ec.uilogic.navigation.DashboardScreens
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.uilogic.serializer.UiSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class AddDocumentInteractorPartialState {
    data class Success(val options: List<DocumentOptionItemUi>) :
        AddDocumentInteractorPartialState()

    data class Failure(val error: String) : AddDocumentInteractorPartialState()
}

interface AddDocumentInteractor {
    fun getAddDocumentOption(flowType: IssuanceFlowUiConfig): Flow<AddDocumentInteractorPartialState>

    fun issueDocument(
        issuanceMethod: IssuanceMethod,
        documentType: DocType
    ): Flow<IssueDocumentPartialState>

    fun addSampleData(): Flow<AddSampleDataPartialState>

    fun handleUserAuth(
        context: Context,
        crypto: BiometricCrypto,
        notifyOnAuthenticationFailure: Boolean,
        resultHandler: DeviceAuthenticationResult
    )

    fun buildGenericSuccessRouteForDeferred(flowType: IssuanceFlowUiConfig): String

    fun resumeOpenId4VciWithAuthorization(uri: String)

    fun getDummyData(): List<ExpandableListItemData<Event>>
}

class AddDocumentInteractorImpl(
    private val walletCoreDocumentsController: WalletCoreDocumentsController,
    private val deviceAuthenticationInteractor: DeviceAuthenticationInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
) : AddDocumentInteractor {
    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getAddDocumentOption(flowType: IssuanceFlowUiConfig): Flow<AddDocumentInteractorPartialState> =
        flow {
            val options = mutableListOf(
                DocumentOptionItemUi(
                    text = DocumentIdentifier.PID.toUiName(resourceProvider),
                    icon = AppIcons.Id,
                    type = DocumentIdentifier.PID,
                    available = true
                ),
                DocumentOptionItemUi(
                    text = DocumentIdentifier.MDL.toUiName(resourceProvider),
                    icon = AppIcons.Id,
                    type = DocumentIdentifier.MDL,
                    available = canCreateExtraDocument(flowType)
                ),
                DocumentOptionItemUi(
                    text = DocumentIdentifier.AGE.toUiName(resourceProvider),
                    icon = AppIcons.Id,
                    type = DocumentIdentifier.AGE,
                    available = canCreateExtraDocument(flowType)
                ),
                DocumentOptionItemUi(
                    text = DocumentIdentifier.PHOTOID.toUiName(resourceProvider),
                    icon = AppIcons.Id,
                    type = DocumentIdentifier.PHOTOID,
                    available = canCreateExtraDocument(flowType)
                )
            )
            if (flowType == IssuanceFlowUiConfig.NO_DOCUMENT) {
                options.add(
                    DocumentOptionItemUi(
                        text = DocumentIdentifier.SAMPLE.toUiName(resourceProvider),
                        icon = AppIcons.Id,
                        type = DocumentIdentifier.SAMPLE,
                        available = true
                    )
                )
            }
            emit(
                AddDocumentInteractorPartialState.Success(
                    options = options
                )
            )
        }.safeAsync {
            AddDocumentInteractorPartialState.Failure(
                error = it.localizedMessage ?: genericErrorMsg
            )
        }

    override fun issueDocument(
        issuanceMethod: IssuanceMethod,
        documentType: DocType
    ): Flow<IssueDocumentPartialState> =
        walletCoreDocumentsController.issueDocument(
            issuanceMethod = issuanceMethod,
            documentType = documentType
        )

    override fun addSampleData(): Flow<AddSampleDataPartialState> =
        walletCoreDocumentsController.addSampleData()

    override fun handleUserAuth(
        context: Context,
        crypto: BiometricCrypto,
        notifyOnAuthenticationFailure: Boolean,
        resultHandler: DeviceAuthenticationResult
    ) {
        deviceAuthenticationInteractor.getBiometricsAvailability {
            when (it) {
                is BiometricsAvailability.CanAuthenticate -> {
                    deviceAuthenticationInteractor.authenticateWithBiometrics(
                        context = context,
                        crypto = crypto,
                        notifyOnAuthenticationFailure = notifyOnAuthenticationFailure,
                        resultHandler = resultHandler
                    )
                }

                is BiometricsAvailability.NonEnrolled -> {
                    deviceAuthenticationInteractor.launchBiometricSystemScreen()
                }

                is BiometricsAvailability.Failure -> {
                    resultHandler.onAuthenticationFailure()
                }
            }
        }
    }

    override fun buildGenericSuccessRouteForDeferred(flowType: IssuanceFlowUiConfig): String {
        val navigation = when (flowType) {
            IssuanceFlowUiConfig.NO_DOCUMENT -> ConfigNavigation(
                navigationType = NavigationType.PushRoute(
                    route = DashboardScreens.Dashboard.screenRoute,
                    popUpToRoute = IssuanceScreens.AddDocument.screenRoute
                ),
            )

            IssuanceFlowUiConfig.EXTRA_DOCUMENT -> ConfigNavigation(
                navigationType = NavigationType.PopTo(
                    screen = DashboardScreens.Dashboard
                )
            )
        }
        val successScreenArguments = getSuccessScreenArgumentsForDeferred(navigation)
        return generateComposableNavigationLink(
            screen = CommonScreens.Success,
            arguments = successScreenArguments
        )
    }

    override fun resumeOpenId4VciWithAuthorization(uri: String) {
        walletCoreDocumentsController.resumeOpenId4VciWithAuthorization(uri)
    }

    override fun getDummyData(): List<ExpandableListItemData<Event>> {
        val collapsedItem1 = ListItemData<Event>(
            event = Event.CollapsedItemClicked("collapsed_id1"),
            itemId = "collapsed_id1",
            mainText = "Collapsed Title",
            supportingText = "Click to expand",
            trailingContentData = ListItemTrailingContentData.Icon(
                iconData = AppIcons.KeyboardArrowDown
            ),
        )

        val expandedItems1 = listOf(
            ListItemData<Event>(
                event = Event.ExpandedItemClicked("item11"),
                itemId = "item11",
                mainText = "Expanded Item 11",
                supportingText = "Details about item 11",
                trailingContentData = ListItemTrailingContentData.Checkbox(
                    checkboxData = CheckboxData(
                        isChecked = false,
                        enabled = true,
                        onCheckedChange = null
                    )
                )
            ),
            ListItemData<Event>(
                event = Event.ExpandedItemClicked("item12"),
                itemId = "item12",
                mainText = "Expanded Item 12",
                supportingText = "Details about item 12",
                trailingContentData = ListItemTrailingContentData.Checkbox(
                    checkboxData = CheckboxData(
                        isChecked = true,
                        enabled = true,
                        onCheckedChange = {
                            println("Giannis checkbox clicked")
                        }
                    )
                )
            )
        )

        val collapsedItem2 = ListItemData<Event>(
            event = Event.CollapsedItemClicked("collapsed_id2"),
            itemId = "collapsed_id2",
            mainText = "Collapsed Title",
            supportingText = "Click to expand",
            trailingContentData = ListItemTrailingContentData.Icon(
                iconData = AppIcons.KeyboardArrowDown
            ),
        )

        val expandedItems2 = listOf(
            ListItemData<Event>(
                event = Event.ExpandedItemClicked("item21"),
                itemId = "item21",
                mainText = "Expanded Item 21",
                supportingText = "Details about item 21",
                trailingContentData = ListItemTrailingContentData.Checkbox(
                    checkboxData = CheckboxData(
                        isChecked = false,
                        enabled = true,
                        onCheckedChange = null
                    )
                )
            ),
            ListItemData<Event>(
                event = Event.ExpandedItemClicked("item22"),
                itemId = "item22",
                mainText = "Expanded Item 22",
                supportingText = "Details about item 22",
                trailingContentData = ListItemTrailingContentData.Checkbox(
                    checkboxData = CheckboxData(
                        isChecked = true,
                        enabled = true,
                        onCheckedChange = {
                            println("Giannis checkbox clicked")
                        }
                    )
                )
            )
        )

        return listOf(
            ExpandableListItemData(
                collapsed = collapsedItem1,
                expanded = expandedItems1,
            ),
            ExpandableListItemData(
                collapsed = collapsedItem2,
                expanded = expandedItems2,
            ),
        )
    }

    private fun getSuccessScreenArgumentsForDeferred(
        navigation: ConfigNavigation
    ): String {
        val (headerConfig, imageConfig, buttonText) = Triple(
            first = SuccessUIConfig.HeaderConfig(
                title = resourceProvider.getString(R.string.issuance_add_document_deferred_success_title),
                color = ThemeColors.warning
            ),
            second = SuccessUIConfig.ImageConfig(
                type = SuccessUIConfig.ImageConfig.Type.DRAWABLE,
                drawableRes = AppIcons.ClockTimer.resourceId,
                tint = ThemeColors.warning,
                contentDescription = resourceProvider.getString(AppIcons.ClockTimer.contentDescriptionId)
            ),
            third = resourceProvider.getString(R.string.issuance_add_document_deferred_success_primary_button_text)
        )

        return generateComposableArguments(
            mapOf(
                SuccessUIConfig.serializedKeyName to uiSerializer.toBase64(
                    SuccessUIConfig(
                        headerConfig = headerConfig,
                        content = resourceProvider.getString(R.string.issuance_add_document_deferred_success_subtitle),
                        imageConfig = imageConfig,
                        buttonConfig = listOf(
                            SuccessUIConfig.ButtonConfig(
                                text = buttonText,
                                style = SuccessUIConfig.ButtonConfig.Style.PRIMARY,
                                navigation = navigation
                            )
                        ),
                        onBackScreenToNavigate = navigation,
                    ),
                    SuccessUIConfig.Parser
                ).orEmpty()
            )
        )
    }

    private fun canCreateExtraDocument(flowType: IssuanceFlowUiConfig): Boolean =
        flowType != IssuanceFlowUiConfig.NO_DOCUMENT
}