package com.github.openwebnet.service.impl;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.github.openwebnet.component.Injector;
import com.github.openwebnet.model.DeviceModel;
import com.github.openwebnet.repository.DeviceRepository;
import com.github.openwebnet.service.CommonService;
import com.github.openwebnet.service.DeviceService;
import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.openwebnet.model.DeviceModel.Status.FAIL;
import static com.github.openwebnet.model.DeviceModel.Status.SUCCESS;

public class DeviceServiceImpl implements DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    @Inject
    DeviceRepository deviceRepository;

    @Inject
    CommonService commonService;

    public DeviceServiceImpl() {
        Injector.getApplicationComponent().inject(this);
    }


    @Override
    public Observable<String> add(DeviceModel device) {
        return deviceRepository.add(device);
    }

    @Override
    public Observable<Void> update(DeviceModel device) {
        return deviceRepository.update(device);
    }

    @Override
    public Observable<Void> delete(String uuid) {
        return deviceRepository.delete(uuid);
    }

    @Override
    public Observable<DeviceModel> findById(String uuid) {
        return deviceRepository.findById(uuid);
    }

    @Override
    public Observable<List<DeviceModel>> findByEnvironment(Integer id) {
        return deviceRepository.findByEnvironment(id);
    }

    @Override
    public Observable<DeviceModel> sendRequest(DeviceModel device) {
        return commonService.findClient(device.getGatewayUuid())
            .send(() -> device.getRequest())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(openSession -> Joiner.on("")
                .join(Stream.of(openSession.getResponse())
                    .map(response -> response.getValue())
                    .collect(Collectors.toList())))
            .map(response -> {
                boolean isExpectedResponse = device.getResponse().equals(response);
                device.setStatus(isExpectedResponse ? SUCCESS: FAIL);
                log.debug("SEND_REQUEST: [isExpected={}][response={}][value={}]",
                    isExpectedResponse, device.getResponse(), response);
                return device;
            })
            .onErrorReturn(throwable1 -> {
                log.error("fail to send request for device={}", device.getUuid());
                device.setStatus(null);
                return device;
            });
    }
}