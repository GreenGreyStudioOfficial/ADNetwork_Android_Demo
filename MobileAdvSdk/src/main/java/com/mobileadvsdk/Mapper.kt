package com.mobileadvsdk

import com.mobileadvsdk.datasource.domain.model.*
import com.mobileadvsdk.datasource.remote.model.*
import org.json.JSONArray
import org.json.JSONObject

internal fun DeviceInfo.toRemote(): AdvDataRequestRemote = AdvDataRequestRemote(
    id,
    test,
    imp.map { it.toRemote() },
    app.toRemote(),
    device.toRemote(),
    user.toRemote()
)

internal fun Imp.toRemote(): ImpRemote = ImpRemote(id, video?.toRemote(), banner?.toRemote(), instl)

internal fun Video.toRemote(): VideoRemote = VideoRemote(mimes, w, h, ext.toRemote())
internal fun Banner.toRemote(): BannerRemote = BannerRemote(mimes, w, h, ext.toRemote(), api)
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
    Bid(id, impid, nurl, lurl, adm, cid, crid, api, extAdv?.toDomain())

internal fun ExtAdvRemote.toDomain(): ExtAdv =
    ExtAdv(cache_max, cache_timeout, req_timeout, imp_timeout)

internal fun JSONObject.getStringOrNull(key: String): String? =
    if (isNull(key)) null else getString(key)

internal fun JSONObject.getLongOrNull(key: String): Long? = if (isNull(key)) null else getLong(key)
internal fun JSONObject.getIntOrNull(key: String): Int? = if (isNull(key)) null else getInt(key)
internal fun JSONObject.getJsonObjectOrNull(key: String): JSONObject? = if (isNull(key)) null else getJSONObject(key)
internal fun JSONObject.getJsonArrayOrNull(key: String): JSONArray? = if (isNull(key)) null else getJSONArray(key)


internal fun String.toAdvDataRemote(): AdvDataRemote {
    val json = JSONObject(this)
    val id = json.getString("id")
    val bidid = json.getStringOrNull("bidid")
    val arr = json.getJsonArrayOrNull("seatbid")
    val list = mutableListOf<SeatbidRemote>()
    arr?.let {
        for (i in 0 until it.length()) {
            val str = it[i]
            list.add(str.toString().toSeatbidRemote())
        }
    }

    return AdvDataRemote(id, bidid, list)
}

internal fun String.toSeatbidRemote(): SeatbidRemote = JSONObject(this)
    .run {
        val arr = getJsonArrayOrNull("bid")
        val list = mutableListOf<BidRemote>()
        arr?.let {
            for (i in 0 until it.length()) {
                val str = it[i].toString()
                list.add(str.toBidRemote())
            }
        }
        SeatbidRemote(list)
    }

internal fun String.toBidRemote(): BidRemote = JSONObject(this)
    .run {
        val id = getString("id")
        val impid = getStringOrNull("impid")
        val nurl = getStringOrNull("nurl")
        val lurl = getStringOrNull("lurl")
        val adm = getString("adm")
        val cid = getStringOrNull("cid")
        val crid = getStringOrNull("crid")
        val api = getIntOrNull("api")
        val extAdv = getJsonObjectOrNull("ext")?.toString()?.toExtAdvRemote()
        BidRemote(id, impid, nurl, lurl, adm, cid, crid, api, extAdv)
    }

internal fun String.toExtAdvRemote(): ExtAdvRemote = JSONObject(this)
    .run {
        val cache_max = getLongOrNull("cache_max")
        val cache_timeout = getLongOrNull("cache_timeout")
        val req_timeout = getLongOrNull("req_timeout")
        val imp_timeout = getLongOrNull("imp_timeout")
        ExtAdvRemote(cache_max, cache_timeout, req_timeout, imp_timeout)
    }

internal fun String.toAdvData(): AdvData = JSONObject(this)
    .run {
        val id = getString("id")
        val advertiseType = getString("advertiseType")
        val bidid = getStringOrNull("bidid")
        val arr = getJsonArrayOrNull("seatbid")
        val list = mutableListOf<Seatbid>()
        arr?.let {
            for (i in 0 until it.length()) {
                val str = it[i].toString()
                list.add(str.toSeatbid())
            }
        }
        AdvData(
            id,
            if (advertiseType == "REWARDED") AdvertiseType.REWARDED else AdvertiseType.INTERSTITIAL,
            bidid,
            list
        )
    }

internal fun String.toSeatbid() = JSONObject(this)
    .run {
        val arr = getJsonArrayOrNull("bid")
        val list = mutableListOf<Bid>()
        arr?.let {
            for (i in 0 until it.length()) {
                val str = it[i].toString()
                list.add(str.toBid())
            }
        }
        Seatbid(list)
    }

internal fun String.toBid() = JSONObject(this)
    .run {
        val id = getString("id")
        val impid = getStringOrNull("impid")
        val nurl = getStringOrNull("nurl")
        val lurl = getStringOrNull("lurl")
        val adm = getString("adm")
        val cid = getStringOrNull("cid")
        val crid = getStringOrNull("crid")
        val api = getIntOrNull("api")
        val extAdv: ExtAdv? = getStringOrNull("extAdv")?.toExtAdv()
        Bid(id, impid, nurl, lurl, adm, cid, crid, api, extAdv)
    }

internal fun String.toExtAdv() = JSONObject(this)
    .run {
        val cache_max = getLongOrNull("cache_max")
        val cache_timeout = getLongOrNull("cache_timeout")
        val req_timeout = getLongOrNull("req_timeout")
        val imp_timeout = getLongOrNull("imp_timeout")
        ExtAdv(cache_max, cache_timeout, req_timeout, imp_timeout)
    }

internal fun AdvertiseType.toJson()= when(this){
    AdvertiseType.INTERSTITIAL -> "INTERSTITIAL"
    AdvertiseType.REWARDED -> "REWARDED"
}

