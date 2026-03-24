package com.example.androidfronted.data.mapper;

import com.example.androidfronted.data.local.entity.CertificationEntity;
import com.example.androidfronted.data.model.CertInfoResponse;

public class CertificationMapper {
    public static CertificationEntity fromCertInfoResponse(CertInfoResponse response) {
        if (response == null || response.getData() == null) {
            return null;
        }

        CertInfoResponse.CertInfoData data = response.getData();
        CertInfoResponse.UserCert userCert = data.getUserCert();
        CertInfoResponse.WorkCert workCert = data.getWorkCert();
        CertInfoResponse.TriCert triCert = data.getTriCert();
        CertInfoResponse.ImmovablesCert immovablesCert = data.getImmovablesCert();

        return new CertificationEntity(
                (int) userCert.getUserId(),
                userCert.getIdCard(),
                userCert.getCreditScore(),
                userCert.getBankCardId(),
                workCert.getWorkCertId(),
                workCert.getEmploymentCertPath(),
                workCert.getSalaryCertPath(),
                triCert.getTriCertId(),
                triCert.getSocialSecurityPath(),
                triCert.getCreditReportPath(),
                immovablesCert.getImmovableCertId(),
                immovablesCert.getPropertyCertPath(),
                immovablesCert.getCarCertPath()
        );
    }
}
