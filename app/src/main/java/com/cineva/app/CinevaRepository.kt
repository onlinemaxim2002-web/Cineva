package com.cineva.app
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
data class Movie(val id:String,val title:String,val description:String?,val poster:String?,val access:String,val year:Int?)
data class Channel(val id:String,val name:String,val logo:String?,val live:Boolean)
class CinevaRepository(c:Context){
 private val p=c.getSharedPreferences("cineva",0); private val http=OkHttpClient()
 private val base=BuildConfig.SUPABASE_URL; private val key=BuildConfig.SUPABASE_KEY
 private var token:String? get()=p.getString("token",null); set(v){p.edit().putString("token",v).apply()}
 private suspend fun call(path:String,method:String="GET",body:String?=null,auth:Boolean=true)=withContext(Dispatchers.IO){
  val b=Request.Builder().url(base+path).addHeader("apikey",key); if(auth&&token!=null)b.addHeader("Authorization","Bearer $token")
  if(body!=null)b.method(method,body.toRequestBody("application/json".toMediaType())); val r=http.newCall(b.build()).execute(); val s=r.body?.string().orEmpty(); if(!r.isSuccessful)error(s); s
 }
 suspend fun login(e:String,pw:String){val o=JSONObject(call("/auth/v1/token?grant_type=password","POST",JSONObject().put("email",e).put("password",pw).toString(),false));token=o.getString("access_token")}
 suspend fun signup(e:String,pw:String,n:String){val o=JSONObject(call("/auth/v1/signup","POST",JSONObject().put("email",e).put("password",pw).put("data",JSONObject().put("display_name",n)).toString(),false));o.optString("access_token").takeIf{it.isNotEmpty()}?.let{token=it}}
 fun loggedIn()=token!=null; fun logout(){p.edit().clear().apply()}
 suspend fun movies():List<Movie>{val a=JSONArray(call("/rest/v1/movies?select=id,title,description,poster_path,access_level,year&status=eq.published&order=featured.desc,sort_order.asc"));return List(a.length()){val o=a.getJSONObject(it);Movie(o.getString("id"),o.getString("title"),o.optString("description").ifBlank{null},o.optString("poster_path").ifBlank{null},o.getString("access_level"),o.optInt("year").takeIf{it>0})}}
 suspend fun channels():List<Channel>{val a=JSONArray(call("/rest/v1/channels?select=id,name,logo_path,is_live&status=eq.published&order=is_live.desc,sort_order.asc"));return List(a.length()){val o=a.getJSONObject(it);Channel(o.getString("id"),o.getString("name"),o.optString("logo_path").ifBlank{null},o.optBoolean("is_live"))}}
}
