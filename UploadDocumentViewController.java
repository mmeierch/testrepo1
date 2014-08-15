package com.ubs.swidPAW.v1.printerbasket.ctrl;

import static com.google.common.collect.Collections2.transform;
import static com.ubs.swidMLV.v1.printerbasket.datamodel.BasketItemContentType.PDF;
import static com.ubs.swidMLV.v1.printerbasket.datamodel.PrintStatus.N;
import static com.ubs.swidPAW.v1.printerbasket.UBSMessages.docs_upload_success_message;
import static com.ubs.swidPAW.v1.printerbasket.UBSMessages.message_illegal_cque_access;
import static com.ubs.swidPAW.v1.printerbasket.UBSMessages.upload_error;
import static com.ubs.swidPAW.v1.util.MessageUtils.addMessage;
import static com.ubs.swidUW1.v2.ubswidgets.spring.UbsMessage.addMessage;
import static com.ubs.swidUW1.v2.ubswidgets.spring.UbsMessageType.CONFIRMATION_HEADER;
import static com.ubs.swidUW1.v2.ubswidgets.spring.UbsMessageType.ITEM;
import static com.ubs.swidUW1.v2.ubswidgets.spring.UbsMessageType.WARNING_HEADER;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ubs.swidMLV.v1.dt.BasketStatus;
import com.ubs.swidMLV.v1.dt.PdfFormResponse;
import com.ubs.swidMLV.v1.dt.PdfFormResponse.PdfFormResponseErrorCode;
import com.ubs.swidMLV.v1.dt.partner.BusinessRelationship;
import com.ubs.swidMLV.v1.dt.partner.Partner;
import com.ubs.swidMLV.v1.intf.PdfUtilsService;
import com.ubs.swidMLV.v1.printerbasket.datamodel.BasketContext;
import com.ubs.swidMLV.v1.printerbasket.datamodel.BasketDocument;
import com.ubs.swidMLV.v1.printerbasket.datamodel.BasketItem;
import com.ubs.swidMLV.v1.printerbasket.datamodel.DocumentType;
import com.ubs.swidMLV.v1.printerbasket.datamodel.util.ScdaAccessHelper;
import com.ubs.swidMLV.v1.referencedata.base.DynamicEnum;
import com.ubs.swidMLV.v1.referencedata.dynamic.DocumentCategoryEnum;
import com.ubs.swidMLV.v1.referencedata.dynamic.DocumentTypeManualUploadEnum;
import com.ubs.swidMLV.v1.referencedata.dynamic.FormSupportEacceptEnum;
import com.ubs.swidMLV.v1.util.SecurityContextHelper;
import com.ubs.swidPAW.v1.printerbasket.ValidationMessages;
import com.ubs.swidPAW.v1.printerbasket.ViewConstants;
import com.ubs.swidPAW.v1.printerbasket.command.UploadDocumentViewCommand;
import com.ubs.swidPAW.v1.printerbasket.util.widgets.BusinessRelationshipFormatter;
import com.ubs.swidPAW.v1.printerbasket.util.widgets.ListModelBean;
import com.ubs.swidPAW.v1.printerbasket.util.widgets.StringToListModelBeanTransformer;
import com.ubs.swidUW1.v2.ubswidgets.model.list.IListModelData;
import com.ubs.swidUW1.v2.ubswidgets.model.list.ListModel;
import com.ubs.swidUW1.v2.ubswidgets.spring.UbsMessage;

@Controller
@RequestMapping(value = "/uploadForm.htm")
public class UploadDocumentViewController extends AbstractBasketViewBaseController {

	protected static final String UPLOAD_FORM = "uploadForm";
	protected static final String UPLOAD_PROCESS_NAME = "Manual Upload";

	private PdfUtilsService pdfUtilsService;

	@RequestMapping()
	public ModelAndView getUploadForm(HttpServletRequest request, @ModelAttribute Partner partner, UploadDocumentViewCommand command) {

		if (command.getFormSelected() == null && command.getDocTypeSelected() == null) {
			// set default to DocTypeSelected
			command.setDocTypeSelected(true);
		}

		ModelAndView mav = new ModelAndView(UPLOAD_FORM);
		prepareDropDowns(mav, partner, command);
		mav.addObject(command.getViewBeanName(), command);

		return mav;
	}

	@ResponseBody
	@RequestMapping(method = POST, params = { ViewConstants.URL_PARAM_UPLOAD_DOCUMENT_CHECK_FORM_ID })
	public String checkFormId(HttpServletRequest request, RedirectAttributes attributes, @ModelAttribute Partner partner,
			@Valid UploadDocumentViewCommand command, BindingResult result, HttpSession session) throws Exception {

		if (command.getFile() == null) {
			// No form could be found:
			return "";
		}

		final String formId = pdfUtilsService.extractFormId(command.getFile().getBytes());
		if (formId == null || formId.isEmpty()) {
			// PDF is NOT unflattened:
			return "";
		} else if (formId.equals("unflattened")) {
			// PDF is unflattened but no form ID has been found:
			return "unflattened:" + ValidationMessages.uploadDocumentViewCommand_formid_notavailable;
		}

		// PDF is unflattened and a form ID has been found:
		if (isKnownId(formId)) {
			// We know a form with this ID:
			return formId;
		} else {
			// The ID is unknown:
			return "unflattened:" + ValidationMessages.uploadDocumentViewCommand_formid_nomappingfound;
		}
	}

	private boolean isKnownId(final String formId) {
		final List<?> validIds = DynamicEnum.getDynamicEnumsBy(FormSupportEacceptEnum.class);
		for (final Object id : validIds) {
			// Sidenote: The 'DynamicEnum' class is quite broken when it comes to generics! This is why we must iterate over Objects and do
			// explicit casts, otherwise we would inevitably get numerous generics warnings here!
			final DynamicEnum<?, ?> e = (DynamicEnum<?, ?>) id;
			final String v = e.getKey();
			if (formId.equals(v)) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping(method = POST, params = { ViewConstants.URL_PARAM_UPLOAD_DOCUMENT_UPLOAD })
	public ModelAndView handleUpload(HttpServletRequest request, RedirectAttributes attributes, @ModelAttribute Partner partner,
			@Valid UploadDocumentViewCommand command, BindingResult result, HttpSession session) throws Exception {

		// handle automatic errors via annotations
		if (result.hasErrors()) {
			UbsMessage.saveErrors(request, result);
			return getUploadForm(request, partner, command);
		}

		// handle docType and form errors manually because depending on checkbox-seleciont
		Boolean formSelected = command.getFormSelected() != null && command.getFormSelected();
		Boolean docTypeSelected = command.getDocTypeSelected() != null && command.getDocTypeSelected();
		if (docTypeSelected && command.getSelectedDocumentType() == null) {
			ModelAndView mav = getUploadForm(request, partner, command);
			addMessage(mav, WARNING_HEADER, ValidationMessages.NotEmpty_uploadDocumentViewCommand_selectedDocumentType);
			return mav;
		}

		if (!formSelected && !docTypeSelected) {
			// nothing selected -> try to extract formResponse from PDF
			// and check some errors.
			formSelected = true;
		}

		PdfFormResponse eFormResponseToUse = null;
		if (formSelected) {
			eFormResponseToUse = pdfUtilsService.extractPdfFormResponse(command.getFile().getBytes());
			if (PdfFormResponseErrorCode.EXPECTED_ERROR_NOT_AN_UNFLATTENED_EFORM.equals(eFormResponseToUse.getErrorCode())) {
				// is an unflattened document -> manually set data to use
				if (command.getForm() == null) {
					ModelAndView mav = getUploadForm(request, partner, command);
					addMessage(mav, WARNING_HEADER, ValidationMessages.NotEmpty_uploadDocumentViewCommand_selectedForm);
					return mav;
				}

				eFormResponseToUse = new PdfFormResponse(command.getForm().value(), command.getFile().getBytes(), command.getForm()
						.getKey());
			} else {
				// use eFormResponseToUse
			}
		} else {
			// remove form if DocType is selected.
			command.setForm(null);
		}

		if (eFormResponseToUse != null && eFormResponseToUse.getErrorCode() != null) {
			// check eFormResponse-Errors
			PdfFormResponseErrorCode code = eFormResponseToUse.getErrorCode();
			if (code.equals(PdfFormResponseErrorCode.NO_CORRECT_FORM_ID_AVAILABLE)) {
				ModelAndView mav = getUploadForm(request, partner, command);
				addMessage(mav, WARNING_HEADER, ValidationMessages.uploadDocumentViewCommand_formid_notavailable,
						new Object[] { eFormResponseToUse.getFormId() });
				return mav;
			} else if (code.equals(PdfFormResponseErrorCode.NO_MAPPING_TO_FORM_AVAILABLE)) {
				ModelAndView mav = getUploadForm(request, partner, command);
				addMessage(mav, WARNING_HEADER, ValidationMessages.uploadDocumentViewCommand_formid_nomappingfound);
				return mav;
			} else if (code.equals(PdfFormResponseErrorCode.PDF_FLATTENING_ERROR)) {
				ModelAndView mav = getUploadForm(request, partner, command);
				addMessage(mav, WARNING_HEADER, ValidationMessages.uploadDocumentViewCommand_formid_flatteningerror,
						new Object[] { eFormResponseToUse.getFormId() });
				return mav;
			}
		}

		BasketStatus status = persistItem(partner, command, eFormResponseToUse);
		Set<ConstraintViolation<?>> violations = status.getConstraintViolations();

		ModelAndView mav = null;
		if (violations.size() > 0) {
			mav = new ModelAndView(UPLOAD_FORM);
			addMessage(mav, WARNING_HEADER, upload_error);
			for (ConstraintViolation<?> constraintViolation : violations) {
				// String key = constraintViolation.getMessageTemplate().replace("{", "").replace("}", "");
				addMessage(mav, ITEM, constraintViolation.getMessage(), new Object[] { constraintViolation.getPropertyPath(),
						constraintViolation.getInvalidValue() });
			}
			prepareDropDowns(mav, partner, command);
			mav.addObject(command.getViewBeanName(), command);
		} else {

			addMessage(attributes, CONFIRMATION_HEADER, docs_upload_success_message);
			attributes.addAttribute("id", command.getId());
			attributes.addAttribute("printStatus", N);
			return new ModelAndView("redirect:/displayBasket.htm");
		}

		return mav;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ModelAndView createIllegalDocumentAccessView(AccessDeniedException ex, HttpServletRequest request) {
		addMessage(request, WARNING_HEADER, message_illegal_cque_access);
		return new ModelAndView("message");
	}

	private BasketStatus persistItem(Partner partner, UploadDocumentViewCommand command, PdfFormResponse eFormResponseToUse)
			throws IOException {
		BusinessRelationship businessRelationship = partner.getBusinessRelationship(command.getSelectedInternalBusinessRelationshipId());
		BasketContext basketContext = basketService.getBasketContext(businessRelationship.getPartnerId(), businessRelationship
				.getInternalBusinessRelationshipId());

		BasketDocument doc = new BasketDocument();

		MultipartFile uploadedFile = command.getFile();

		if (eFormResponseToUse != null) {
			// use flattened PDF from PdfFormResponse
			doc.setDocument(eFormResponseToUse.getFlattenedPdf());
		} else {
			// use selected PDF
			doc.setDocument(command.getFile().getBytes());
		}

		BasketItem item = new BasketItem();

		String title = StringUtils.isNotEmpty(command.getTitle()) ? command.getTitle() : uploadedFile.getOriginalFilename();
		item.setTitle(title);
		item.setContentType(PDF);

		// required fields

		String docSens = ScdaAccessHelper.createScdaString(null, null);
		item.setDocumentSensitivities(docSens);

		// SET DocumentType
		if (eFormResponseToUse != null) {
			// use DocumentType form eForm
			DocumentType type = eFormResponseToUse.getForm().getDocumentType();
			item.setDocumentType((type == null ? null : type.getId()));
		} else {
			// use selectedDocumentType
			item.setDocumentType(command.getSelectedDocumentType().getKey());
		}

		// SET Form
		if (eFormResponseToUse != null) {
			// set form if available
			item.setFormId(eFormResponseToUse.getForm().getId());
		}

		// SET DocumentCategory
		if (eFormResponseToUse != null) {
			// use category from eFrom
			item.setDocumentCategory(eFormResponseToUse.getForm().getDocumentCategory());
		} else {
			// use category from selectedDocumentType
			String categoryId = command.getSelectedDocumentType().value().getDocCategoryId();
			item.setDocumentCategory(DocumentCategoryEnum.INSTANCE.getReferenceDataByKey(categoryId).value());
		}

		item.setSourceGPN(SecurityContextHelper.getSourceTnumber());
		item.setSourceProcess(UPLOAD_PROCESS_NAME);
		item.setSourceSoftware(SecurityContextHelper.getFirstAuthorizationId());

		// Setting the connections between the data objects
		item.setBasketDocument(doc);
		doc.setBasketItem(item);
		item.setBasketContext(basketContext);

		item.setPurposeOfDocument(command.getPurpose());

		return basketService.addBasketItemList(Arrays.asList(new BasketItem[] { item }));
	}

	private void prepareDropDowns(ModelAndView mav, Partner partner, UploadDocumentViewCommand command) {
		List<BusinessRelationship> businessRelationshipIds = partner.getBusinessRelationships();
		if (businessRelationshipIds.size() > 1) {
			IListModelData businessRelationshipDropDownModel = createBusinessRelationshipDropDownModel(businessRelationshipIds);
			mav.addObject("businessRelationshipModel", businessRelationshipDropDownModel);
		} else {
			command.setSelectedInternalBusinessRelationshipId(businessRelationshipIds.get(0).getInternalBusinessRelationshipId());
		}
		mav.addObject("documentTypeModel", createDropDownModel(DocumentTypeManualUploadEnum.INSTANCE.values()));
		mav.addObject("purposeModel", createPurposeDropDownModel(partner));
		mav.addObject("formModel", createDropDownModel(FormSupportEacceptEnum.INSTANCE.values()));
	}

	private IListModelData createBusinessRelationshipDropDownModel(List<BusinessRelationship> businessRelationshipIds) {
		// pass the properties key and value of Map.Entry for display (value) and selection (key)
		IListModelData listModel = new ListModel(null, "internalBusinessRelationshipId", new BusinessRelationshipFormatter());
		listModel.setData(businessRelationshipIds);
		return listModel;
	}

	private ListModel createPurposeDropDownModel(Partner partner) {
		List<String> purposeList = basketService.getPurposeList(partner);

		// retrieve all purposes for this specific basket context and show change purpose GUI
		ListModel model = new ListModel("value");
		model.setData(new ArrayList<ListModelBean>(transform(purposeList, StringToListModelBeanTransformer.getInstance())));
		return model;
	}

	public void setPdfUtilsService(PdfUtilsService pdfUtilsService) {
		this.pdfUtilsService = pdfUtilsService;
	}

}