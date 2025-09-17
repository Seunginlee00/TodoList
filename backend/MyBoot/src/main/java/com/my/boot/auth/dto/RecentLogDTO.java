package com.my.boot.auth.dto;

public record RecentLogDTO (
     String date,
     String time,
     String os,
     String browser,
     Long count
){

}
