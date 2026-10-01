package com.cineva.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val Black=Color(0xFF08090C)
private val Card=Color(0xFF15161B)
private val Red=Color(0xFFE50914)

class MainActivity:ComponentActivity(){
 override fun onCreate(state:Bundle?){super.onCreate(state);setContent{MaterialTheme(colorScheme=darkColorScheme(primary=Red,background=Black,surface=Card)){CinevaApp(CinevaRepository(this))}}}
}

@Composable
fun CinevaApp(repo:CinevaRepository){
 var logged by remember{mutableStateOf(repo.loggedIn())}
 if(!logged){AuthScreen(repo){logged=true};return}
 var tab by remember{mutableIntStateOf(0)}
 var movies by remember{mutableStateOf(emptyList<Movie>())}
 var channels by remember{mutableStateOf(emptyList<Channel>())}
 LaunchedEffect(Unit){movies=runCatching{repo.movies()}.getOrDefault(emptyList());channels=runCatching{repo.channels()}.getOrDefault(emptyList())}
 Scaffold(containerColor=Black,bottomBar={
  NavigationBar(containerColor=Card){
   listOf("Home" to Icons.Default.Home,"Channels" to Icons.Default.LiveTv,"Search" to Icons.Default.Search,"Profile" to Icons.Default.Person).forEachIndexed{i,item->
    NavigationBarItem(tab==i,{tab=i},{Icon(item.second,item.first)},{Text(item.first)})
   }
  }
 }){pad->Box(Modifier.fillMaxSize().padding(pad)){when(tab){
  0->Home(movies)
  1->Channels(channels)
  2->Search(movies)
  3->Profile{repo.logout();logged=false}
 }}}
}

@Composable fun Home(movies:List<Movie>){
 LazyColumn(Modifier.fillMaxSize().background(Black),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Text("Cineva",fontSize=34.sp,fontWeight=FontWeight.Bold);Text("Stream what you love.",color=Color.LightGray)}
  item{Text("Movies",fontSize=22.sp,fontWeight=FontWeight.SemiBold)}
  items(movies,key={it.id}){m->MovieCard(m)}
  if(movies.isEmpty())item{Text("No published movies yet.",color=Color.Gray)}
 }
}

@Composable fun MovieCard(m:Movie){
 Card(Modifier.fillMaxWidth().clickable{},colors=CardDefaults.cardColors(containerColor=Card)){
  Row(Modifier.padding(10.dp)){AsyncImage(m.poster,m.title,Modifier.size(92.dp,132.dp),contentScale=ContentScale.Crop);Column(Modifier.padding(start=14.dp)){Text(m.title,fontSize=20.sp,fontWeight=FontWeight.Bold);m.year?.let{Text(it.toString(),color=Color.Gray)};Spacer(Modifier.height(8.dp));AssistChip({},label={Text(if(m.access=="premium")"PREMIUM" else "FREE")});m.description?.let{Text(it,maxLines=3,color=Color.LightGray)}}}
 }
}

@Composable fun Channels(channels:List<Channel>){
 LazyColumn(Modifier.fillMaxSize().background(Black),contentPadding=PaddingValues(16.dp)){item{Text("Live Channels",fontSize=28.sp,fontWeight=FontWeight.Bold)};items(channels){c->ListItem({Text(c.name)},{Text(if(c.live)"LIVE NOW" else "Offline")},{AsyncImage(c.logo,null,Modifier.size(52.dp),contentScale=ContentScale.Crop)},colors=ListItemDefaults.colors(containerColor=Black))}}
}

@Composable fun Search(movies:List<Movie>){
 var q by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().background(Black).padding(16.dp)){Text("Search",fontSize=28.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));OutlinedTextField(q,{q=it},Modifier.fillMaxWidth(),label={Text("Search movies")});LazyColumn{items(movies.filter{it.title.contains(q,true)}){Text(it.title,Modifier.padding(16.dp))}}}
}

@Composable fun Profile(onLogout:()->Unit){
 Column(Modifier.fillMaxSize().background(Black).padding(24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Person,null,Modifier.size(64.dp));Text("Your Cineva account",fontSize=24.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(24.dp));Button(onLogout){Text("Log out")}}
}

@Composable fun AuthScreen(repo:CinevaRepository,success:()->Unit){
 var signup by remember{mutableStateOf(false)};var name by remember{mutableStateOf("")};var email by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope()
 Column(Modifier.fillMaxSize().background(Black).padding(24.dp),verticalArrangement=Arrangement.Center){Text("Cineva",fontSize=46.sp,fontWeight=FontWeight.Bold);Text(if(signup)"Create your account" else "Welcome back",color=Color.LightGray);Spacer(Modifier.height(28.dp));if(signup){OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Name")});Spacer(Modifier.height(10.dp))};OutlinedTextField(email,{email=it},Modifier.fillMaxWidth(),label={Text("Email")});Spacer(Modifier.height(10.dp));OutlinedTextField(pass,{pass=it},Modifier.fillMaxWidth(),label={Text("Password")});Spacer(Modifier.height(18.dp));Button(!busy,{scope.launch{busy=true;runCatching{if(signup)repo.signup(email.trim(),pass,name.trim())else repo.login(email.trim(),pass)}.onSuccess{success()};busy=false}},Modifier.fillMaxWidth()){Text(if(busy)"Please wait…" else if(signup)"Create account" else "Log in")};TextButton({signup=!signup}){Text(if(signup)"Already have an account? Log in" else "New to Cineva? Create account")}}
}
