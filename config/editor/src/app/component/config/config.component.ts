import {Component} from '@angular/core';
import {appRoutingPaths} from "../../shared/app-routing-paths.config";
import {AppConfigFromYamlService} from "../../shared/app-config-from-yaml.service";
import {ExtensionsService} from "../../modules/config-editor/extensions.service";
import {Router} from "@angular/router";
import {parse} from "yaml";
import {AppConfig} from "../../definition/AppConfig";
import {MatSnackBar} from "@angular/material/snack-bar";
import {environment} from "../../../environments/environment";
import {plainToInstance} from 'class-transformer';
import {EnvroinmentDef} from 'src/app/envroinment.def';
import {from, map, switchMap, zipAll} from "rxjs";
import {HttpClient} from "@angular/common/http";
import * as JSZip from "jszip";

@Component({
    selector: 'app-config',
    templateUrl: './config.component.html',
    styleUrls: ['./config.component.scss']
})
export class ConfigComponent {
    title = 'sbc2ha';
    fileContent: string | ArrayBuffer | null = null;

    links = appRoutingPaths


    constructor(private as: AppConfigFromYamlService,
                private es: ExtensionsService,
                private httpClient: HttpClient,
                private router: Router,
                private snackBar: MatSnackBar) {
    }

    private getFilesFromEvent(e: Event) {
        let files: File[] = [];
        if (e.target) {
            const t: FileEvent = e?.target as FileEvent;
            if (t.files.length) {
                files = Array.from(t.files);
            }
        }
        return files;
    }

    public readFile(e: Event) {
        const files = this.getFilesFromEvent(e)

        from(files)
            .pipe(
                map((file: File) => {
                    return from(file.text())
                }),
                zipAll(),
                switchMap(value => {
                    if (files.length == 1) {
                        const file = files[0];
                        if (file.type == 'application/x-yaml') {
                            return this.uploadFileToConvert(file)
                        } else if (file.type == 'application/zip') {
                            return this.uploadFileToConvert(file)
                        } else {
                            throw "bad file type";
                        }
                    } else {
                        return this.createZip(files, value).pipe(
                            switchMap(zip => {
                                return this.uploadFileToConvert(zip)
                            })
                        )
                    }
                })
            )
            .subscribe({
                    next: value => {
                        const ac: AppConfig = plainToInstance(AppConfig, value);
                        //const ac: AppConfig = this.as.fromYaml(value);
                        let uuid = this.es.saveConfig(ac);
                        this.es.setConfig(ac)
                        this.router.navigate(["/editor"], {queryParams: {uuid}});
                    },
                    error: err => {
                        this.snackBar.open("error: " + err.error)
                    }
                }
            );

    }

    private createZip(files: File[], value: string[]) {

        const zip = new JSZip();
        files.forEach(file => {
            zip.file(file.name, file)
        })

        return from(zip.generateAsync({type: 'blob'}))
            .pipe(
                map(zip => {
                    return new File([zip], 'config.zip')
                }))


    }

    public uploadYamlFile(e: Event) {
        const files = this.getFilesFromEvent(e);
        if (files.length == 1) {
            let file1 = files[0];
            if (file1.type == 'application/x-yaml') {
                from(file1.text())
                    .subscribe(fileContent => {
                        let parsed = parse(fileContent);
                        const ac: AppConfig = this.as.fromYaml(parsed);
                        let uuid = this.es.saveConfig(ac);
                        this.es.setConfig(ac)
                        this.router.navigate(["/editor"], {queryParams: {uuid}});
                    })
            }
        }

        /*
                // @ts-ignore
                let fileList: any = (e as {
                    files: any
                }).files;
                let file = fileList[0];
                let fileReader: FileReader = new FileReader();
                let self = this;
                fileReader.onloadend = function (x) {
                    self.fileContent = fileReader.result;
                    if (file.type == 'application/x-yaml') {
                        let parsed = parse(self.fileContent as string);
                        const ac: AppConfig = self.as.fromYaml(parsed);
                        let uuid = self.es.saveConfig(ac);
                        self.es.setConfig(ac)
                        self.router.navigate(["/editor"], {queryParams: {uuid}});
                    }
                }
                fileReader.readAsText(file);*/
    }

    public uploadJsonFile(e: Event) {
        const files = this.getFilesFromEvent(e);
        if (files.length == 1) {
            let file1 = files[0];
            if (file1.type == 'application/json') {
                from(file1.text())
                    .subscribe(fileContent => {
                        try {
                            let parsed = parse(fileContent);
                            const ac: AppConfig = plainToInstance(AppConfig, parsed);
                            let uuid = this.es.saveConfig(ac);
                            this.es.setConfig(ac)
                            this.router.navigate(["/editor"], {queryParams: {uuid}});
                        } catch (e) {
                            //console.error(e)
                            this.snackBar.open("error converting json to config")
                        }
                    })
            }
        }
        /*
               // @ts-ignore
               let fileList: any = (e as {
                   files: any
               }).files;
               let file = fileList[0];
               let fileReader: FileReader = new FileReader();
               let self = this;
               fileReader.onloadend = function (x) {
                   self.fileContent = fileReader.result;
                   if (file.type == 'application/json') {
                       let parsed = parse(self.fileContent as string);
                       const ac: AppConfig = plainToInstance(AppConfig, parsed);
                       let uuid = self.es.saveConfig(ac);
                       self.es.setConfig(ac)
                       self.router.navigate(["/editor"], {queryParams: {uuid}});
                   }
               }
               fileReader.readAsText(file);*/
    }

    downloadConfig() {
        if (environment.webOnly!) {
            this.webOnly();
        } else {
            this.es.getCurrentConfig()
                .subscribe(ac => {
                    this.es.setConfig(ac);
                    this.router.navigate(["/editor"], {queryParams: {config: "current"}});
                })
        }
    }

    webOnly() {
        this.snackBar.open("unavailable in webOnly preview", "Ok, i understand", {panelClass: 'error'})
    }


    private uploadFileToConvert(fileToUpload: File) {

        const endpoint = '/api/convert';
        const formData: FormData = new FormData();
        formData.append('config', fileToUpload, fileToUpload.name);
        return this.httpClient
            .post(endpoint, formData)

    }


    protected readonly environment: EnvroinmentDef = environment;


    private readFileAsText(blob: Blob) {
        return from(blob.text())
    }
}


interface FileEvent extends EventTarget {
    files: FileList
}
