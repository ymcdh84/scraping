package com.scraping;

import com.database.mariadb.TaxDataSaveService;

import java.text.SimpleDateFormat;
import java.util.*;

public class TaxBillScraping {

    public static void main(String[] args) throws Exception {

        //[STEP-0] Config Class Load and  TaxService 생성
        Configuration conf = new Configuration();

        TaxService taxService = new TaxService(conf.jetsRoot, conf.xmlFileDir);

        //[STEP-1] 현재 날짜
        Date today = new Date();
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        String toDate = "yyyyMMddHHmmss"; //hhmmss로 시간,분,초만 뽑기도 가능
        SimpleDateFormat formatter = new SimpleDateFormat(toDate,currentLocale);

        //[STEP-2] 인증서 정보
        Map<String, Object> keys = taxService.certificateKey(conf.certRoot);

        String cert = keys.get("pemCert").toString();
        String pri = keys.get("pemPriv").toString();

        //[STEP-3] 홈택스 서명 문자열 호출 API
        Map<String, Object> sign = taxService.urlHometaxWqAction();

        String wmonId = sign.get("wmonId").toString();
        String txppSessionId = sign.get("txppSessionId").toString();
        String pkcEncSsn = sign.get("pkcEncSsn").toString();

        //[STEP-4] 서명값 함수 호출
        String sigedResult = taxService.signedResult(conf.certRoot, pkcEncSsn, conf.pw);

        //[STEP-5] logSgnt 조합 생성
        String inputLogSgnt = pkcEncSsn + "$"
                + conf.snNum + "$"
                + formatter.format(today) + "$"
                + sigedResult;

        String bLogSgnt = Base64.getEncoder().encodeToString(inputLogSgnt.getBytes());

        //[STEP-6] randomEnc 생성
        String randomEnc = taxService.randomPrivateKey(cert.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "")
                , pri.replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "").replace("-----END ENCRYPTED PRIVATE KEY-----", "")
                , conf.idn
                , conf.pw
        );

        //[STEP-7] 홈택스 로그인 함수 호출
        Map loginMap = new HashMap<>();

        loginMap.put("wmonId", wmonId);
        loginMap.put("txppSessionId", txppSessionId);
        loginMap.put("pkcEncSsn", pkcEncSsn);
        loginMap.put("cert", cert);
        loginMap.put("logSgnt", bLogSgnt);
        loginMap.put("pkcLgnClCd", "04");
        loginMap.put("pkcLoginYnImpv", "Y");
        loginMap.put("randomEnc", randomEnc);

        Map<String, Object> login = taxService.urlHometaxPubcLogin(loginMap);

        // **로그인 성공 후에 WMONID,세션 ID를 새로 받는다
        String loginWmonId = login.get("wmonId").toString();
        String loginTxppSessionId = login.get("txppSessionId").toString();

        //[STEP-8] 로그인 사용자 정보 api 호출 -> tin값 얻기 위해
        Map loginInfoMap = new HashMap<>();

        loginInfoMap.put("wmonId", loginWmonId);
        loginInfoMap.put("txppSessionId", loginTxppSessionId);

        Map<String, Object> loginInfo = taxService.urlHometaxLoginInfo(loginInfoMap);

        String tin = loginInfo.get("tin").toString();

        //[STEP-9] 전자세금 계산서 세션 함수 호출 -> TEETSessionID 값을 얻기 위해
        Map<String, Object> teetSessionInfo = taxService.urlHometaxTEETSessionID(loginInfoMap);

        String loginTeetSessionId = teetSessionInfo.get("teetSessionId").toString();

        //[STEP-10] SSO 토큰 함수 호출
        Map ssoMap = new HashMap<>();
        ssoMap.put("wmonId", loginWmonId);
        ssoMap.put("txppSessionId", loginTxppSessionId);
        ssoMap.put("teetSessionId", loginTeetSessionId);

        String ssoToken = taxService.urlHometaxSsoToken(ssoMap);

        //[STEP-11] SSO 로그인 (전자세금계산서 시스템 로그인)
        ssoMap.put("ssoToken", ssoToken);
        String ssoTeetSessionId = taxService.urlHometaxSsoLogin(ssoMap);

        //[STEP-12] NetFunnelId 취득 함수 호출
        ssoMap.put("teetSessionId", ssoTeetSessionId);
        String netFunnelId = taxService.urlHometaxNetFunnelId(ssoMap);

        //[STEP-13] 전자 세금계산서 조회
        ssoMap.put("netFunnelId", netFunnelId);
        ssoMap.put("tin", tin);
        String taxList = taxService.urlHometaxSchTaxBill(ssoMap, 1, "UTEETBDA01");

        //[STEP-14] 전자 계산서 조회
        taxList += taxService.urlHometaxSchTaxBill(ssoMap, 1, "UTEETBDA09");

        //[STEP-15] 세금계산서 xml 파일 다운로드
        List<String> taxIssueIdList = taxService.downLoadTaxBillXml(ssoMap, taxList);

        //[STEP-16] 세금계산서 xml 파일 파싱 결과 리턴
        TaxParsingService taxParsingService = new TaxParsingService(conf.xmlFileDir);
        List<Map<String, Object>> parseXmlList = taxParsingService.parsingTaxXmlFile(taxIssueIdList);

        //[STEP-17] 세금계산서 저장
        TaxDataSaveService taxDataSaveService = new TaxDataSaveService();
        taxDataSaveService.saveTaxBill(parseXmlList, conf.url, conf.name, conf.dbPw, conf.driver);
    }
}
