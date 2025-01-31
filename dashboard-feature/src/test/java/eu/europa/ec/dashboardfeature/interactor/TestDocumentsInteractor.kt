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

package eu.europa.ec.dashboardfeature.interactor

import eu.europa.ec.commonfeature.util.TestsData.mockedPendingMdlUi
import eu.europa.ec.commonfeature.util.TestsData.mockedPendingPidUi
import eu.europa.ec.corelogic.config.WalletCoreConfig
import eu.europa.ec.corelogic.controller.DeleteDocumentPartialState
import eu.europa.ec.corelogic.controller.IssueDeferredDocumentPartialState
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.corelogic.model.DeferredDocumentData
import eu.europa.ec.corelogic.model.FormatType
import eu.europa.ec.dashboardfeature.controllers.FiltersController
import eu.europa.ec.eudi.wallet.document.Document
import eu.europa.ec.eudi.wallet.document.DocumentId
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import eu.europa.ec.testfeature.mockedExceptionWithMessage
import eu.europa.ec.testfeature.mockedExceptionWithNoMessage
import eu.europa.ec.testfeature.mockedFullDocuments
import eu.europa.ec.testfeature.mockedGenericErrorMessage
import eu.europa.ec.testfeature.mockedPlainFailureMessage
import eu.europa.ec.testlogic.extension.runFlowTest
import eu.europa.ec.testlogic.extension.runTest
import eu.europa.ec.testlogic.extension.toFlow
import eu.europa.ec.testlogic.rule.CoroutineTestRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.emptyFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TestDocumentsInteractor {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var walletCoreDocumentsController: WalletCoreDocumentsController

    @Mock
    private lateinit var filtersController: FiltersController

    @Mock
    private lateinit var walletCoreConfig: WalletCoreConfig

    private lateinit var interactor: DocumentsInteractor

    private lateinit var closeable: AutoCloseable

    private lateinit var mockDocumentId: String
    private lateinit var mockDocumentName: String

    @Before
    fun before() {
        closeable = MockitoAnnotations.openMocks(this)

        interactor = DocumentsInteractorImpl(
            resourceProvider = resourceProvider,
            walletCoreDocumentsController = walletCoreDocumentsController,
            filtersController = filtersController,
            walletCoreConfig = walletCoreConfig,
        )

        whenever(resourceProvider.genericErrorMessage()).thenReturn(mockedGenericErrorMessage)

        mockDocumentId = "mockDocumentId"
        mockDocumentName = "mockDocumentName"
    }

    @After
    fun after() {
        closeable.close()
    }

    // region deleteDocument
    // Case 1:
    // walletCoreDocumentsController.getAllDocuments() returns a list of Documents
    // with a size of two.

    // Case 1 Expected Result:
    // DocumentInteractorDeleteDocumentPartialState.SingleDocumentDeleted state.
    @Test
    fun `Given Case 1, When deleteDocument is called, Then Case 1 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            whenever(walletCoreDocumentsController.getAllDocuments())
                .thenReturn(mockedFullDocuments)
            assert(walletCoreDocumentsController.getAllDocuments().size == 2)
            mockDeleteDocumentCall(response = DeleteDocumentPartialState.Success)

            // When
            interactor.deleteDocument(mockDocumentId).runFlowTest {
                // Then
                val expectedFlow =
                    DocumentInteractorDeleteDocumentPartialState.SingleDocumentDeleted

                assertEquals(expectedFlow, awaitItem())
            }
        }
    }

    // Case 2:
    // walletCoreDocumentsController.getAllDocuments() returns an empty list.

    // Case 2 Expected Result:
    // DocumentInteractorDeleteDocumentPartialState.AllDocumentsDeleted state
    @Test
    fun `Given Case 2, When deleteDocument is called, Then Case 2 Expected Result is returned`() {
        coroutineRule.runTest {
            // Given
            val mockDocumentsList = mock<List<Document>>()
            whenever(walletCoreDocumentsController.getAllDocuments()).thenReturn(mockDocumentsList)
            whenever(mockDocumentsList.isEmpty()).thenReturn(true)
            assert(walletCoreDocumentsController.getAllDocuments().isEmpty())

            mockDeleteDocumentCall(response = DeleteDocumentPartialState.Success)

            // When
            interactor.deleteDocument(mockDocumentId).runFlowTest {
                val expectedFlow = DocumentInteractorDeleteDocumentPartialState.AllDocumentsDeleted

                // Then
                assertEquals(expectedFlow, awaitItem())
            }
        }
    }

    // Case 3:
    // walletCoreDocumentsController.getAllDocuments() returns Failure

    // Case 3 Expected Result:
    // DocumentInteractorDeleteDocumentPartialState.Failure state.
    @Test
    fun `Given Case 3, When deleteDocument is called, Then Case 3 Expected Result is returned`() =
        coroutineRule.runTest {
            // Given
            mockDeleteDocumentCall(
                response = DeleteDocumentPartialState.Failure(
                    errorMessage = mockedPlainFailureMessage
                )
            )

            // When
            interactor.deleteDocument(mockDocumentId).runFlowTest {
                // Then
                assertEquals(
                    DocumentInteractorDeleteDocumentPartialState.Failure(
                        errorMessage = mockedPlainFailureMessage
                    ),
                    awaitItem()
                )
            }
        }

    // Case 4:
    // walletCoreDocumentsController.deleteDocument() throws an exception with a message.

    // Case 4 Expected Result:
    // DocumentInteractorDeleteDocumentPartialState.Failure state with exception's localized message.
    @Test
    fun `Given Case 4, When deleteDocument is called, Then Case 4 Expected Result is returned`() =
        coroutineRule.runTest {
            // Given
            whenever(walletCoreDocumentsController.deleteDocument(mockDocumentId))
                .thenThrow(mockedExceptionWithMessage)

            // When
            interactor.deleteDocument(mockDocumentId).runFlowTest {
                // Then
                assertEquals(
                    DocumentInteractorDeleteDocumentPartialState.Failure(
                        errorMessage = mockedExceptionWithMessage.localizedMessage!!
                    ),
                    awaitItem()
                )
            }
        }

    // Case 5:
    // walletCoreDocumentsController.deleteDocument() throws an exception with no message.

    // Case 5 Expected Result:
    // DocumentInteractorDeleteDocumentPartialState.Failure state with the generic error message.
    @Test
    fun `Given Case 5, When deleteDocument is called, Then Case 5 Expected Result is returned`() =
        coroutineRule.runTest {
            // Given
            whenever(walletCoreDocumentsController.deleteDocument(mockDocumentId))
                .thenThrow(mockedExceptionWithNoMessage)

            // When
            interactor.deleteDocument(mockDocumentId).runFlowTest {
                // Then
                assertEquals(
                    DocumentInteractorDeleteDocumentPartialState.Failure(
                        errorMessage = mockedGenericErrorMessage
                    ),
                    awaitItem()
                )
            }
        }
    //endregion

    //region tryIssuingDeferredDocumentsFlow

    // Case 1:
    // When issueDeferredDocument was called:
    // 1. IssueDeferredDocumentPartialState.Issued was emitted, with
    //  - successData, the successfully issued deferred document's DeferredDocumentData, and also,
    // 2. IssueDeferredDocumentPartialState.Failed was emitted, with
    //  - documentId, the failed deferred document's DocumentId.

    // Case 1 Expected Result:
    // DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Result state, with
    //  - successfullyIssuedDeferredDocuments: a list with the successfully issued deferred document's DeferredDocumentData,
    //  - failedIssuedDeferredDocuments: a list with the failed deferred document's DocumentId.
    @Test
    fun `Given Case 1, When tryIssuingDeferredDocumentsFlow is called, Then Case 1 Expected Result is returned`() =
        coroutineRule.runTest {
            // Given
            val mockDeferredPendingDocId1 = mockedPendingPidUi.documentId
            val mockDeferredPendingType1 = mockedPendingPidUi.documentIdentifier.formatType
            val mockDeferredPendingName1 = mockedPendingPidUi.documentName

            val mockDeferredPendingDocId2 = mockedPendingMdlUi.documentId
            val mockDeferredPendingType2 = mockedPendingMdlUi.documentIdentifier.formatType

            val deferredDocuments: Map<DocumentId, FormatType> = mapOf(
                mockDeferredPendingDocId1 to mockDeferredPendingType1,
                mockDeferredPendingDocId2 to mockDeferredPendingType2
            )
            val successData = DeferredDocumentData(
                documentId = mockDeferredPendingDocId1,
                formatType = mockDeferredPendingType1,
                docName = mockDeferredPendingName1
            )

            mockIssueDeferredDocumentCall(
                docId = mockDeferredPendingDocId1,
                response = IssueDeferredDocumentPartialState.Issued(
                    deferredDocumentData = successData
                )
            )
            mockIssueDeferredDocumentCall(
                docId = mockDeferredPendingDocId2,
                response = IssueDeferredDocumentPartialState.Failed(
                    documentId = mockDeferredPendingDocId2,
                    errorMessage = mockedPlainFailureMessage
                )
            )

            // When
            interactor.tryIssuingDeferredDocumentsFlow(deferredDocuments)
                .runFlowTest {
                    // Then
                    val expectedResult =
                        DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Result(
                            successfullyIssuedDeferredDocuments = listOf(successData),
                            failedIssuedDeferredDocuments = listOf(mockDeferredPendingDocId2)
                        )
                    assertEquals(expectedResult, awaitItem())
                }
        }

    // Case 2:
    // IssueDeferredDocumentPartialState.Expired was emitted when issueDeferredDocument was called.

    // Case 2 Expected Result:
    // DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Result with,
    // - successfullyIssuedDeferredDocuments = emptyList.
    // - failedIssuedDeferredDocuments = emptyList.
    @Test
    fun `Given Case 2, When tryIssuingDeferredDocumentsFlow is called, Then Case 2 Expected Result is returned`() =
        coroutineRule.runTest {
            // Given
            val mockDeferredExpiredDocId = mockedPendingPidUi.documentId
            val mockDeferredExpiredDocType = mockedPendingPidUi.documentIdentifier.formatType

            val deferredDocuments: Map<DocumentId, FormatType> = mapOf(
                mockDeferredExpiredDocId to mockDeferredExpiredDocType
            )

            mockIssueDeferredDocumentCall(
                docId = mockDeferredExpiredDocId,
                response = IssueDeferredDocumentPartialState.Expired(
                    documentId = mockDeferredExpiredDocId
                )
            )

            // When
            interactor.tryIssuingDeferredDocumentsFlow(deferredDocuments)
                .runFlowTest {
                    // Then
                    val expectedResult =
                        DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Result(
                            successfullyIssuedDeferredDocuments = emptyList(),
                            failedIssuedDeferredDocuments = emptyList()
                        )
                    assertEquals(expectedResult, awaitItem())
                }
        }

    // Case 3:
    // IssueDeferredDocumentPartialState.NotReady was emitted when issueDeferredDocument was called.

    // Case 3 Expected Result:
    // DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Result with,
    // - successfullyIssuedDeferredDocuments = emptyList.
    // - failedIssuedDeferredDocuments = emptyList.
    @Test
    fun `Given Case 3, When tryIssuingDeferredDocumentsFlow is called, Then Case 3 Expected Result is returned`() =
        coroutineRule.runTest {
            val mockDeferredPendingDocId = mockedPendingPidUi.documentId
            val mockDeferredPendingType = mockedPendingPidUi.documentIdentifier.formatType
            val mockDeferredPendingName = mockedPendingPidUi.documentName

            val deferredDocuments: Map<DocumentId, FormatType> = mapOf(
                mockDeferredPendingDocId to mockDeferredPendingType
            )
            val successData = DeferredDocumentData(
                documentId = mockDeferredPendingDocId,
                formatType = mockDeferredPendingType,
                docName = mockDeferredPendingName
            )

            mockIssueDeferredDocumentCall(
                docId = mockDeferredPendingDocId,
                response = IssueDeferredDocumentPartialState.NotReady(
                    deferredDocumentData = successData
                )
            )

            // When
            interactor.tryIssuingDeferredDocumentsFlow(deferredDocuments).runFlowTest {
                // Then
                val expectedResult =
                    DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Result(
                        successfullyIssuedDeferredDocuments = emptyList(),
                        failedIssuedDeferredDocuments = emptyList()
                    )
                assertEquals(expectedResult, awaitItem())
            }
        }

    // Case 4:
    // walletCoreDocumentsController.issueDeferredDocument() throws an exception with a message.

    // Case 4 Expected Result:
    // DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Failure state with exception's localized message.
    @Test
    fun `Given Case 4, When tryIssuingDeferredDocumentsFlow is called, Then Case 4 Expected Result is returned`() =
        coroutineRule.runTest {
            // Given
            val mockDeferredPendingDocId = mockedPendingPidUi.documentId
            val mockDeferredPendingType = mockedPendingPidUi.documentIdentifier.formatType

            val deferredDocuments: Map<DocumentId, FormatType> = mapOf(
                mockDeferredPendingDocId to mockDeferredPendingType
            )
            whenever(walletCoreDocumentsController.issueDeferredDocument(mockDeferredPendingDocId))
                .thenThrow(mockedExceptionWithMessage)

            // When
            interactor.tryIssuingDeferredDocumentsFlow(deferredDocuments).runFlowTest {
                // Then
                assertEquals(
                    DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Failure(
                        errorMessage = mockedExceptionWithMessage.localizedMessage!!
                    ),
                    awaitItem()
                )
            }
        }

    // Case 5:
    // walletCoreDocumentsController.issueDeferredDocument() throws an exception with no message.

    // Case 5 Expected Result:
    // DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Failure state with the generic error message.
    @Test
    fun `Given Case 5, When tryIssuingDeferredDocumentsFlow is called, Then Case 5 Expected Result is returned`() =
        coroutineRule.runTest {
            // Given
            val mockDeferredPendingDocId = mockedPendingPidUi.documentId
            val mockDeferredPendingType = mockedPendingPidUi.documentIdentifier.formatType

            val deferredDocuments: Map<DocumentId, FormatType> = mapOf(
                mockDeferredPendingDocId to mockDeferredPendingType
            )
            whenever(walletCoreDocumentsController.issueDeferredDocument(mockDeferredPendingDocId))
                .thenThrow(mockedExceptionWithNoMessage)

            // When
            interactor.tryIssuingDeferredDocumentsFlow(deferredDocuments).runFlowTest {
                // Then
                assertEquals(
                    DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Failure(
                        errorMessage = mockedGenericErrorMessage
                    ),
                    awaitItem()
                )
            }
        }

    // Case 6:
    // emptyFlow was returned when issueDeferredDocument was called.

    // Case 6 Expected Result:
    // DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Result state.
    @Test
    fun `Given Case 6, When tryIssuingDeferredDocumentsFlow is called, Then Case 6 Expected Result is returned`() =
        coroutineRule.runTest {
            // Given
            val mockDeferredPendingDocId = mockedPendingPidUi.documentId
            val mockDeferredPendingType = mockedPendingPidUi.documentIdentifier.formatType

            val deferredDocuments: Map<DocumentId, FormatType> = mapOf(
                mockDeferredPendingDocId to mockDeferredPendingType
            )
            whenever(walletCoreDocumentsController.issueDeferredDocument(mockDeferredPendingDocId))
                .thenReturn(emptyFlow())

            // When
            interactor.tryIssuingDeferredDocumentsFlow(deferredDocuments).runFlowTest {
                // Then
                val expectedResult =
                    DocumentInteractorRetryIssuingDeferredDocumentsPartialState.Result(
                        successfullyIssuedDeferredDocuments = emptyList(),
                        failedIssuedDeferredDocuments = listOf(mockDeferredPendingDocId)
                    )
                assertEquals(expectedResult, awaitItem())
            }
        }

    //endregion

    //region Mock Calls of the Dependencies
    private fun mockDeleteDocumentCall(response: DeleteDocumentPartialState) {
        whenever(walletCoreDocumentsController.deleteDocument(anyString()))
            .thenReturn(response.toFlow())
    }

    private fun mockIssueDeferredDocumentCall(
        docId: DocumentId,
        response: IssueDeferredDocumentPartialState
    ) {
        whenever(walletCoreDocumentsController.issueDeferredDocument(docId))
            .thenReturn(response.toFlow())
    }
    //endregion
}