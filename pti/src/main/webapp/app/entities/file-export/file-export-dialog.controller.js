(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FileExportDialogController', FileExportDialogController);

    FileExportDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'FileExport', 'ExportTable', 'PrametreExport', 'FileExportParam'];
    function FileExportDialogController($timeout, $scope, $stateParams, $uibModalInstance, entity, FileExport, ExportTable, PrametreExport, FileExportParam) {
        var vm = this;

        vm.fileExport = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.exportTables = ExportTable.queryAll();
        vm.afficherListParam = afficherListParam;
        vm.afficherList = false;
        vm.ligneParam = [];
        vm.opLogiqueTab = [];
        vm.addField = addField;
        vm.operateurs = ["EGALE", "SUPERIEUR", "INFEREUR", "SUPERIEUR OU EGALE", "INFEREUR OU EGALE"];
        vm.choix = ["VRAI", "FAUX"];
        vm.i = 0;
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.paramObjList = [];
        vm.deleteParam = deleteParam;
        vm.listeStatiqueValeur =[];
        vm.listeDynamiqueValeur =[];



        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        

        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function afficherListParam() {
            vm.afficherList = true;
            vm.paramsExporter = PrametreExport.queryByExportTable({ id: vm.fileExport.exportTable.id });
        }

        // loadParams(); 

        function addField() {
            vm.parametre = PrametreExport.get({ id: vm.param }, function (result) {
                vm.i=vm.i+1;
                if(result.nomChamps && result.nomChamps.includes(';')){
                    vm.col = result.nomChamps.split(';');
                }
                
                if(result.typeListe==="LISTE_DYNAMIQUE"){
                    FileExport.getTableValue({tableName:result.tableRef,  colonne:result.nomChamps}, function (result) {
                        for(var j=0; j<result.length; j++) {
                            vm.obj = {
                                label:result[j][vm.col[1]],
                                valeur:result[j][vm.col[0]]
                            }
                           
                            vm.listeDynamiqueValeur.push(vm.obj);
                        }
                        console.log(vm.obj)                       
                        
                    });
                }
                if(result.typeListe==="LISTE_STATIQUE"){
                    vm.res = result.valeur.split(';');
                    for(var j=0; j<vm.res.length; j++) {
                        var elt = vm.res[j].split(':');
                        vm.obj = {
                            label:elt[0],
                            valeur:elt[1]
                        }
                        vm.listeStatiqueValeur.push(vm.obj);
                    }
                }
                if (vm.ligneParam.length > 0) {
                    vm.ligneParam.push([
                        {
                            type: 'opLogique',
                            optr: 'false',
                            opLogiqNgModel: "opL_" + result.nomParametre,
                        },
                        {

                            title: result.nomParametre,
                            type: result.typeChamps,
                            optr: 'true',
                            operateurNgModel: "operateur_" + result.nomParametre,
                            valeurNgModel: "valeur_" + result.nomParametre,
                            obj: result,
                            ligneParamIndex: vm.i,
                            type_liste:result.typeListe
                            
                        },
                    ]);
                } else {
                    vm.ligneParam.push([
                        {
                            
                            title: result.nomParametre,
                            type: result.typeChamps,
                            optr: 'true',
                            operateurNgModel: "operateur_" + result.nomParametre,
                            valeurNgModel: "valeur_" + result.nomParametre,
                            obj: result,
                            ligneParamIndex: vm.i,
                            type_liste:result.typeListe
                        },
                    ]);
                }

            });
        }


        if (vm.fileExport.id) {
            vm.fileExportParams = FileExportParam.queryByFileExport({ id: vm.fileExport.id }).$promise.then(
                function (fileExportParam) {
                    fileExportParam.forEach(function getLigneElt(ligne) {

                        if(ligne.parametreExport.nomChamps && ligne.parametreExport.nomChamps.includes(';')){
                            vm.col = ligne.parametreExport.nomChamps.split(';');
                        }

                        if (ligne.parametreExport.typeChamps == "DATE") {
                            ligne.valeur = new Date(ligne.valeur);
                            console.log(ligne.valeur);
                        } else if (ligne.parametreExport.typeChamps == "NUMBER") {
                            ligne.valeur = +ligne.valeur;
                            console.log(ligne.valeur);
                        }
                        if(ligne.parametreExport.typeListe=="LISTE_DYNAMIQUE"){
                            FileExport.getTableValue({tableName:ligne.parametreExport.tableRef, colonne:ligne.parametreExport.nomChamps}, function (result) {
                                // vm.listeValeur = result
                                // console.log(vm.listeValeur)
                                for(var j=0; j<result.length; j++) {
                                    vm.obj = {
                                        label:result[j][vm.col[1]],
                                        valeur:result[j][vm.col[0]]
                                    }
                                   
                                    vm.listeDynamiqueValeur.push(vm.obj);
                                }
                                // console.log(vm.obj) 
                            });
                            // console.log(vm.listeValeur);
                        }

                        if(ligne.parametreExport.typeListe==="LISTE_STATIQUE"){
                            vm.res = ligne.parametreExport.valeur.split(';');
                            for(var j=0; j<vm.res.length; j++) {
                                var elt = vm.res[j].split(':');
                                if (elt!=""){
                                    vm.obj = {
                                        label:elt[0],
                                        valeur:elt[1]
                                    }
                                    vm.listeStatiqueValeur.push(vm.obj);
                                }
                                
                            }
                        }

                        if (ligne.operateurLogique) {
                            vm.ligneParam.push([
                                {
                                    type: 'opLogique',
                                    optr: 'false',
                                    opLogiqNgModel: ligne.operateurLogique
                                },
                                {
                                    ligneParamIndex: ligne.id,
                                    title: ligne.parametreExport.nomParametre,
                                    type: ligne.parametreExport.typeChamps,
                                    optr: 'true',
                                    operateurNgModel: ligne.operateur,
                                    type_liste:ligne.parametreExport.typeListe,
                                    valeurNgModel: ligne.valeur,
                                    obj: ligne
                                },
                            ]);

                        } else {
                            vm.ligneParam.push([
                                {
                                    ligneParamIndex: ligne.id,
                                    title: ligne.parametreExport.nomParametre,
                                    type: ligne.parametreExport.typeChamps,
                                    optr: 'true',
                                    operateurNgModel: ligne.operateur,
                                    valeurNgModel: ligne.valeur,
                                    obj: ligne,
                                    type_liste:ligne.parametreExport.typeListe,
                                },
                            ]);

                        }
                    })
                }
            );
        }


        function save() {
            vm.isSaving = true;
            if (vm.fileExport.id !== null) {
                if (vm.ligneParam.length > 1) {
                    vm.ligneParam.forEach(
                        function getLigneElt(ln) {
                            ln.forEach(function saveLigneElt(elt1) {
                                FileExportParam.queryByFileExport({ id: vm.fileExport.id }).$promise.then(
                                    function (fileExportParam) {
                                        fileExportParam.forEach(function getLigneElt(ligne) {
                                            if (ln.length == 1) {
                                                if (ligne.id == ln[0].ligneParamIndex) {
                                                    console.log(ln[0]);
                                                    console.log("yes");
                                                    vm.fileParam = {
                                                        id: ligne.id,
                                                        valeur: ln[0].valeurNgModel,
                                                        operateur: ln[0].operateurNgModel,
                                                        operateurLogique: ln[0].opLogiqNgModel,
                                                        fileExport: vm.fileExport,
                                                        parametreExport: ligne.parametreExport
                                                    }
                                                    FileExportParam.update(vm.fileParam, onSaveSuccess, onSaveError);
                                                }

                                            }
                                            else if (ln.length > 1) {
                                                if (ligne.id == ln[1].ligneParamIndex) {
                                                    console.log(ln[1]);
                                                    vm.fileParam = {
                                                        id: ligne.id,
                                                        valeur: ln[1].valeurNgModel,
                                                        operateur: ln[1].operateurNgModel,
                                                        operateurLogique: ln[0].opLogiqNgModel,
                                                        fileExport: vm.fileExport,
                                                        parametreExport: ligne.parametreExport
                                                    }
                                                    FileExportParam.update(vm.fileParam, onSaveSuccess, onSaveError);
                                                }
                                            }


                                        }
                                        )
                                    }
                                )
                            }

                            )

                        })
                } else {
                    FileExportParam.queryByFileExport({ id: vm.fileExport.id }).$promise.then(
                        function (fileExportParam) {
                            fileExportParam.forEach(function getLigneElt(ligne) {
                                console.log(vm.ligneParam.length)
                                vm.ligneParam.forEach(
                                    function getLigneElt(ln) {
                                        ln.forEach(function saveLigneElt(elt1) {
                                            vm.fileParam = {
                                                id: ligne.id,
                                                valeur: elt1.valeurNgModel,
                                                operateur: elt1.operateurNgModel,
                                                operateurLogique: elt1.opLogiqNgModel,
                                                fileExport: vm.fileExport,
                                                parametreExport: ligne.parametreExport
                                            }
                                            console.log(vm.fileParam)
                                        })

                                    },

                                );

                                FileExportParam.update(vm.fileParam, onSaveSuccess, onSaveError);

                            })
                        }
                    )
                }
                FileExport.update(vm.fileExport, onSaveSuccess, onSaveError);

            } else {
                vm.ligneParam.forEach(
                    function buildParamOjectList(ligne) {
                        if (ligne.length == 1) {
                            ligne.forEach(function saveLigneElt(elt) {
                                vm.ligneElt = {
                                    operateur: elt.operateurNgModel,
                                    operateurLogique: null,
                                    valeur: elt.valeurNgModel,
                                    parametreExport: elt.obj,
                                }
                            })
                        }
                        else if (ligne.length > 1) {
                            vm.ligneElt = {
                                operateur: ligne[1].operateurNgModel,
                                operateurLogique: ligne[0].opLogiqNgModel,
                                valeur: ligne[1].valeurNgModel,
                                parametreExport: ligne[1].obj,
                            }
                        }
                        vm.paramObjList.push(vm.ligneElt);

                    }
                )

                vm.fileExportDTO = {
                    fileExport: vm.fileExport,
                    params: vm.paramObjList
                }

                FileExport.save(vm.fileExportDTO, onSaveSuccess, onSaveError);
            }
        }


        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:fileExportUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dateGeneration = false;

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }

        function deleteParamImpl(ligneParamIndex) {
            vm.ligneParam.forEach(
                function checkElement(ligne) {
                    if (ligne.length == 1) {
                        
                        if (ligne[0].ligneParamIndex===ligneParamIndex){
                    
                           
                           if (vm.ligneParam[vm.ligneParam.indexOf(ligne)+1] !==undefined){
                                console.log("----------------------indexSuivante-----------------{}", vm.ligneParam[vm.ligneParam.indexOf(ligne)+1]);
                                vm.ligneParam.splice(vm.ligneParam.indexOf(ligne), 1);
                                vm.ligneParam[vm.ligneParam.indexOf(ligne)+1].splice(vm.ligneParam.indexOf(ligne)+1[0],1);
                           }
                           else{
                                vm.ligneParam.splice(vm.ligneParam.indexOf(ligne), 1);
                           }
                        }
                       
                    }
                    else if (ligne.length > 1) {
                        
                        if (ligne[1].ligneParamIndex===ligneParamIndex){
                            if (vm.ligneParam.indexOf(ligne) !== -1) {
                                vm.ligneParam.splice(vm.ligneParam.indexOf(ligne), 1);
                            }    
                        }
                       
                    }
                }
            )
        }

        function deleteParam(ligneParamIndex) {
            deleteParamImpl(ligneParamIndex);
        }
    }
})();
