;;
;; Windows JOSM NSIS installer language macros
;;

!macro JOSM_MACRO_DEFAULT_STRING LABEL VALUE
  !ifndef "${LABEL}"
    !define "${LABEL}" "${VALUE}"
    !ifdef INSERT_DEFAULT
      !warning "${LANG} lang file mising ${LABEL}, using default.."
    !endif
  !endif
!macroend

!macro JOSM_MACRO_LANGSTRING_INSERT LABEL LANG
  LangString "${LABEL}" "${LANG_${LANG}}" "${${LABEL}}"
  !undef "${LABEL}"
!macroend

!macro JOSM_MACRO_LANGUAGEFILE_BEGIN LANG
  !define CUR_LANG "${LANG}"
!macroend

!macro JOSM_MACRO_LANGUAGEFILE_END
  !define INSERT_DEFAULT
  !include "${JOSM_DEFAULT_LANGFILE}"
  !undef INSERT_DEFAULT

  ; JOSM Language file Version 2
  ; String labels should match those from the default language file.
  
  
  
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_WELCOME_TEXT				${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_DIR_TEXT					${CUR_LANG}

  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_FULL_INSTALL				${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SEC_JOSM 					${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SEC_PLUGINS_GROUP 			${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SEC_TURNRESTRICTIONS_PLUGIN    ${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SEC_WMS 			${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SEC_STARTMENU 				${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SEC_DESKTOP_ICON 			${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SEC_QUICKLAUNCH_ICON 		${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SEC_FILE_EXTENSIONS 		${CUR_LANG}
  
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SECDESC_JOSM				${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SECDESC_PLUGINS_GROUP		${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SECDESC_TURNRESTRICTIONS_PLUGIN	${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SECDESC_WMS			${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SECDESC_STARTMENU			${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SECDESC_DESKTOP_ICON		${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SECDESC_QUICKLAUNCH_ICON	${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_SECDESC_FILE_EXTENSIONS	${CUR_LANG}

  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_UPDATEICONS_ERROR1			${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_UPDATEICONS_ERROR2			${CUR_LANG}
  
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT JOSM_LINK_TEXT					${CUR_LANG}
  
  
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_UNCONFIRMPAGE_TEXT_TOP		${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_DEFAULT_UNINSTALL			${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_FULL_UNINSTALL				${CUR_LANG}
  
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_IN_USE_ERROR				${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_INSTDIR_ERROR				${CUR_LANG}
    
	
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_SEC_UNINSTALL				${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_SEC_PERSONAL_SETTINGS		${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_SEC_PLUGINS					${CUR_LANG}
  
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_SECDESC_UNINSTALL			${CUR_LANG}
  !insertmacro JOSM_MACRO_LANGSTRING_INSERT un.JOSM_SECDESC_PERSONAL_SETTINGS	${CUR_LANG}
  

  !undef CUR_LANG
!macroend

!macro JOSM_MACRO_INCLUDE_LANGFILE LANG FILE
  !insertmacro JOSM_MACRO_LANGUAGEFILE_BEGIN "${LANG}"
  !include "${FILE}"
  !insertmacro JOSM_MACRO_LANGUAGEFILE_END
!macroend
