//
//  ZjhPlugin.h
//  指尖客
//
//  Created by 李超 on 2017/1/7.
//
//

#import <Cordova/CDVPlugin.h>

@interface ZjhPlugin : CDVPlugin

- (void)getAuthInfo:(CDVInvokedUrlCommand *)command;

- (void)systemCopy:(CDVInvokedUrlCommand *)command;

- (void)systemPaste:(CDVInvokedUrlCommand *)command;

- (void)openApp:(CDVInvokedUrlCommand *)command;

- (void)getPackageInfo:(CDVInvokedUrlCommand *)command;

- (void)getTbIdForUrl:(CDVInvokedUrlCommand *)command;

- (void)clearCache:(CDVInvokedUrlCommand *)command;
@end
