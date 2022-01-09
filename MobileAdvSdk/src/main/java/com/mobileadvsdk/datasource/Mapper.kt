package com.mobileadvsdk.datasource

import com.mobileadvsdk.datasource.domain.model.*
import com.mobileadvsdk.datasource.remote.model.*

fun DeviceInfo.toRemote(): AdvDataRequestRemote = AdvDataRequestRemote(
    id,
    test,
    imp.map { it.toRemote() },
    app.toRemote(),
    device.toRemote(),
    user.toRemote()
)

fun Imp.toRemote(): ImpRemote = ImpRemote(id, video.toRemote(), instl)

fun Video.toRemote(): VideoRemote = VideoRemote(mimes, w, h, ext.toRemote())

fun Ext.toRemote(): ExtRemote = ExtRemote(rewarded)

fun AppInfo.toRemote(): AppInfoRemote = AppInfoRemote(id, name, bundle)

fun Device.toRemote(): DeviceRemote = DeviceRemote(
    ua,
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

fun Geo.toRemote(): GeoRemote = GeoRemote(lat, lon, country, region, city)

fun User.toRemote(): UserRemote = UserRemote(id)

fun AdvDataRemote.toDomain(): AdvData = AdvData(id, bidid, seatbid.map { it.toDomain() })

fun SeatbidRemote.toDomain(): Seatbid = Seatbid(bid.map { it.toDomain() })

fun BidRemote.toDomain(): Bid = Bid(id, impid, nurl, lurl, adm, cid, crid, extAdv.toDomain())

fun ExtAdvRemote.toDomain(): ExtAdv = ExtAdv(cache_max, cache_timeout, req_timeout, imp_timeout)

