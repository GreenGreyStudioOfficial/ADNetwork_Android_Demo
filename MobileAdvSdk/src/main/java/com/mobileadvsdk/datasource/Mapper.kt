package com.mobileadvsdk.datasource

import com.mobileadvsdk.datasource.domain.model.*
import com.mobileadvsdk.datasource.remote.model.*

internal fun DeviceInfo.toRemote(): AdvDataRequestRemote = AdvDataRequestRemote(
    id,
    test,
    imp.map { it.toRemote() },
    app.toRemote(),
    device.toRemote(),
    user.toRemote()
)

internal fun Imp.toRemote(): ImpRemote = ImpRemote(id, video.toRemote(), instl)

internal fun Video.toRemote(): VideoRemote = VideoRemote(mimes, w, h, ext.toRemote())

internal fun Ext.toRemote(): ExtRemote = ExtRemote(rewarded)

internal fun AppInfo.toRemote(): AppInfoRemote = AppInfoRemote(id, name, bundle)

internal fun Device.toRemote(): DeviceRemote = DeviceRemote(
    geo.toRemote(),
    ip,
    deviceType,
    make,
    model,
    os,
    osv,
    w,
    h,
    connectionType,
    ifa
)

internal fun Geo.toRemote(): GeoRemote = GeoRemote(lat, lon, country, region, city)

internal fun User.toRemote(): UserRemote = UserRemote(id)

internal fun AdvDataRemote.toDomain(): AdvData =
    AdvData(id, AdvertiseType.REWARDED, bidid, seatbid?.map { it.toDomain() } ?: mutableListOf())

internal fun SeatbidRemote.toDomain(): Seatbid = Seatbid(bid?.map { it.toDomain() } ?: mutableListOf())

internal fun BidRemote.toDomain(): Bid =
    Bid(id, impid, nurl, lurl, adm, cid, crid, extAdv?.toDomain())

internal fun ExtAdvRemote.toDomain(): ExtAdv =
    ExtAdv(cache_max, cache_timeout, req_timeout, imp_timeout)

