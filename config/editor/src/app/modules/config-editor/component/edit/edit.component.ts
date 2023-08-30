import {Component, ViewChild} from '@angular/core';
import {MatAccordion} from "@angular/material/expansion";
import {ActivatedRoute, Router} from "@angular/router";
import {AppConfig} from "../../../../definition/AppConfig";
import {ExtensionsService} from "../../extensions.service";
import {HttpClient} from "@angular/common/http";
import {YamlPipe} from "../../yaml.pipe";
import {JsonDPipe} from "../../json-d.pipe";
import {MatSnackBar} from "@angular/material/snack-bar";
import {LogLevel} from "../../../../definition/enums/LogLevel";
import {saveAs} from "file-saver";


@Component({
    selector: 'app-edit',
    templateUrl: './edit.component.html',
    styleUrls: ['./edit.component.scss']
})
export class EditComponent {
    @ViewChild(MatAccordion) accordion: MatAccordion = {} as MatAccordion

    public appConfig: AppConfig;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private es: ExtensionsService,
                private httpClient: HttpClient,
                private yamlPipe: YamlPipe,
                private jsonPipe: JsonDPipe,
                readonly snackBar: MatSnackBar
    ) {
        if (es.getConfig()) {
            this.appConfig = es.getConfig()
        } else {
            this.appConfig = new AppConfig();
        }
    }

    getConfig() {
        return this.es.getConfig();
    }

    ngOnInit() {
        this.route.queryParams
            .subscribe((params) => {
                if (isConfig(params)) {
                    if (this.es.getConfig() == null) {
                        this.es.getCurrentConfig().subscribe(value => {
                            this.es.setConfig(value);
                            this.appConfig = value;
                        })
                    }

                } else if (isCreator(params)) {
                    this.appConfig.extensionBoards = {
                        vendor: params.v,
                        inputBoard: params.i,
                        outputBoard: params.o
                    }
                    this.es.setConfig(this.appConfig);
                } else if (isSaved(params)) {
                    this.es.loadConfig(params.uuid)
                }
                this.appConfig = this.es.getConfig()
            });
    }

    uploadToCache() {

        const jsonConfig = this.jsonPipe.transform(this.es.getConfig())

        let fileBits: BlobPart[] = [jsonConfig]
        let fileName: string = "cache";

        const fileToUpload = new File(fileBits, fileName);

        const endpoint = '/api/write/cache';
        const formData: FormData = new FormData();
        formData.append('cache', fileToUpload, fileToUpload.name);
        return this.httpClient
            .post(endpoint, formData)
            .subscribe({
                next: value => {
                    this.router.navigate(["/logs"])
                },
                error: err => {
                    this.snackBar.open(err.error)
                }
            });

    }

    uploadToArgument() {

    }

    addLog($event: boolean) {
        this.appConfig.logger = {
            logs: [],
            default: LogLevel.INFO,
        }
    }

    downloadYaml() {
        const data = this.yamlPipe.transform(this.getConfig());
        const theFile = new Blob([data], { type: "text/yaml" });
        saveAs(theFile,"config.yaml")
    }
}

interface EditorParamsCreator {
    v: string;
    i: string;
    o: string;
}

interface EditorParamsConfig {
    config: "current"
}

interface EditorParamsSaved {
    uuid: string;
}

const isCreator = function (object: any): object is EditorParamsCreator {
    return 'v' in object;
}
const isConfig = function (object: any): object is EditorParamsConfig {
    return 'config' in object;
}
const isSaved = function (object: any): object is EditorParamsSaved {
    return 'uuid' in object;
}
