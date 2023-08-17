import {Component} from '@angular/core';
import {appRoutingPaths} from "../../shared/app-routing-paths.config";
import {AppConfigFromYamlService} from "../../shared/app-config-from-yaml.service";
import {ExtensionsService} from "../../modules/config-editor/extensions.service";
import {Router} from "@angular/router";
import {parse} from "yaml";
import {AppConfig} from "../../definition/AppConfig";

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
                private router: Router) {
    }

    public readFile(e: EventTarget | null) {
        // @ts-ignore
        let fileList: any = (e as {
            files: any
        }).files;
        let file = fileList[0];
        let fileReader: FileReader = new FileReader();
        let self = this;
        fileReader.onloadend = function (x) {
            self.fileContent = fileReader.result;
        }
        fileReader.readAsText(file);
    }

    public uploadFile(e: EventTarget | null) {
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
                self.es.setConfig(ac)
                self.router.navigate(["/editor"]);
            }
        }
        fileReader.readAsText(file);
    }

    downloadConfig() {
        this.es.getCurrentConfig()
            .subscribe(ac => {
                this.es.setConfig(ac);
                this.router.navigate(["/editor"],{queryParams:{config:"current"}});
            })
    }
}
