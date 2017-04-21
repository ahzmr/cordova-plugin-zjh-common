//
//  ZjhPlugin.m
//  指尖客
//
//  Created by 李超 on 2017/1/7.
//
//

#import <Cordova/CDVInvokedUrlCommand.h>
#import "ZjhPlugin.h"

@implementation ZjhPlugin

- (void)getAuthInfo:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSArray *arguments = command.arguments;
        NSString *urlStr = @"";
        if (arguments.count) {
            urlStr = arguments[0];
        }
        NSURL *url = [NSURL URLWithString:urlStr];
        NSMutableString *cookiesStr = nil;

        NSHTTPCookieStorage *cookieStorage = [NSHTTPCookieStorage sharedHTTPCookieStorage];
        for (NSHTTPCookie *cookie in [cookieStorage cookiesForURL:url]) {
            if(cookiesStr == nil) {
                cookiesStr = [[NSMutableString alloc] init];
            } else {
                [cookiesStr appendString:@"; "];
            }
            [cookiesStr appendFormat:@"%@=%@", cookie.name, cookie.value];
        }

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:cookiesStr];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)systemCopy:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
        NSArray *arguments = command.arguments;
        if (arguments.count) {
            [pasteboard setString:arguments[0]];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }

    }];
}

- (void)systemPaste:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
        NSString *string = [pasteboard string];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:string];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)openApp:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSArray *arguments = command.arguments;
        if (arguments.count) {
            NSString *appUrlStr = arguments[0];


            NSURL *appUrl = [NSURL URLWithString:appUrlStr];
            NSString *errorMsg = nil;

            if (nil != appUrl && [[UIApplication sharedApplication] canOpenURL:appUrl]) {
                [[UIApplication sharedApplication] openURL:appUrl];

                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            } else {
                if (nil == errorMsg) {
                    errorMsg = @"对应客户端未安装";
                }
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMsg];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
        }
    }];
}

- (void)getPackageInfo:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSDictionary *infoDict = [[NSBundle mainBundle] infoDictionary];
        NSString *versionNum = [infoDict objectForKey:@"CFBundleVersion"];

        CDVPluginResult *result = nil;

        NSDictionary *versionInfo = [NSDictionary dictionaryWithObjectsAndKeys:versionNum, @"versionName", versionNum, @"versionCode", nil];
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:versionInfo];

        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getTbIdForUrl:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSArray *arguments = command.arguments;
        CDVPluginResult *result = nil;
        if (arguments.count == 1) {
            NSString *urlStr = (NSString *) arguments[0];
            if (nil == urlStr || ![urlStr hasPrefix:@"http"]) {
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"商品链接不合法"];
                return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            }
            NSURL *url = [NSURL URLWithString:urlStr];
            NSURLRequest *request = [NSURLRequest requestWithURL:url];
            NSURLResponse *response = [[NSURLResponse alloc] init];
            NSData *data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:nil];
            NSString *str = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];

            BOOL isTM = false;
            NSRange start = [str rangeOfString:@"a.m.taobao.com/i"];
            if (start.location == NSNotFound) {
                start = [str rangeOfString:@"a.m.tmall.com/i"];
                isTM = true;
            }
            if (start.location != NSNotFound) {
                NSUInteger startLoc = NSMaxRange(start);
                NSRange end = [str rangeOfString:@".htm" options:NSCaseInsensitiveSearch range:NSMakeRange(startLoc, 18)];
                if (end.location != NSNotFound) {
                    NSString *tbId = [str substringWithRange:NSMakeRange(startLoc, end.location - startLoc)];
                    if(isTM) {
                        tbId = [[NSString alloc] initWithFormat:@"%@ ", tbId];
                    }
                    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:tbId];
                    return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                }
            } else {
                start = [str rangeOfString:@"var url = '"];
                NSUInteger startLoc = NSMaxRange(start);
                NSUInteger len = [str length] - startLoc;
                NSRange end = [str rangeOfString:@"'" options:NSCaseInsensitiveSearch range:NSMakeRange(startLoc, len)];
                if (end.location != NSNotFound) {
                    NSString *tbUrl = [str substringWithRange:NSMakeRange(startLoc, end.location - startLoc)];
                    NSArray<NSString *> *array = [tbUrl componentsSeparatedByString:@"?"];
                    isTM = [[array objectAtIndex:0] rangeOfString:@"tmall"].location != NSNotFound;

                    NSArray *paramArr = [[array objectAtIndex:1] componentsSeparatedByString:@"&"];
                    NSString *paramStr = nil;
                    for (paramStr in paramArr) {
                        NSArray *keyVal = [paramStr componentsSeparatedByString:@"="];
                        if (keyVal.count == 2 && [keyVal[0] isEqualToString:@"id"]) {
                            NSString *tbId = keyVal[1];
                            if(isTM) {
                                tbId = [[NSString alloc] initWithFormat:@"%@ ", tbId];
                            }
                            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:tbId];
                            return [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                        }
                    }

                }
            }

            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"获取商品ID失败"];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }
    }];
}

- (void)clearCache:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        [[NSURLCache sharedURLCache] removeAllCachedResponses];
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}
@end
