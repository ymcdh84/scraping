package com.scraping;

public class Configuration {

    //경로
    String jetsRoot;//JetKit properties 파일 경로
    String xmlFileDir;//세금계산서 xml파일 저장위치
    String certRoot;//인증서 경로

    //DB 정보
    String driver;
    String url;
    String name;
    String dbPw;

    //인증서 정보
    String snNum;// 인증서 일련번호
    String pw;//비밀번호
    String idn;// 주민번호 혹은 사업자 번호

    //로컬 테스트 Config
//    Configuration(){
//        //경로
//        this.jetsRoot = "C:\\SCOREPKI4J-SERVER-3.5.61-DIST\\data\\config\\tradesign3280.properties";
//        this.xmlFileDir = "C:/tax/";
//        this.certRoot = "C:/Users/IJMAIL/AppData/LocalLow/NPKI/KICA/USER/cn=일진씨앤에스 주식회사,ou=RA센터,ou=기업은행(확대업무),ou=등록기관,ou=licensedCA,o=KICA,c=KR";
//
//        //DB 정보
//        this.driver = "org.mariadb.jdbc.Driver";
//        this.url = "jdbc:mariadb://197.200.11.176:3306/rnd";
//        this.name = "rndadmin";
//        this.dbPw = "rnd1234!!";
//
//        //인증서 정보
//        this.snNum = "060b1600";
//        this.pw = "a1s2d3f4^^";
//        this.idn = "7318601989";
//    }

    //씨앤에스 전자전표 Config
    Configuration(){
        //경로
        this.jetsRoot = "D:\\tax-scraping\\SCOREPKI4J-SERVER-3.5.61-DIST\\data\\config\\tradesign3280.properties";
        this.xmlFileDir = "D:\\tax-scraping\\xml\\";
        this.certRoot = "D:/tax-scraping/certificate/cn=일진씨앤에스 주식회사,ou=RA센터,ou=기업은행(확대업무),ou=등록기관,ou=licensedCA,o=KICA,c=KR";

        //DB 정보
        this.driver = "org.mariadb.jdbc.Driver";
        this.url = "jdbc:mariadb://197.200.11.204:3347/cseacct";
        this.name = "eacct";
        this.dbPw = "Eacct!!00";

        //인증서 정보
        this.snNum = "060b1600";
        this.pw = "a1s2d3f4^^";
        this.idn = "7318601989";
    }

}
