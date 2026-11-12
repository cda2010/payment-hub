(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('EtatOperationController', EtatOperationController);

        EtatOperationController.$inject = ['BASE_URL_API', '$scope', '$state',  'RequestUtils', 'Utils','Operation','TableService'];

        function EtatOperationController( BASE_URL_API, $scope, $state, RequestUtils, Utils, Operation,TableService) {

            var vm = this;
            var URL = BASE_URL_API + "etat/";
            vm.etatIdent=null;
            vm.ctrlOp=false;
            vm.format=null;
            vm.getEtatO = getEtatO;
            vm.genererO=genererO;
            vm.operationStatut=operationStatut;
            vm.item={
                type:null,
                format:null
            };
            vm.etats = [
                {
                    ident:1,
                    libelle:'Opération en attente ',
                    url: URL+'listing/operation',
                    type:'ENATTENTE'

                },
                {
                    ident:2,
                    libelle:'Opération en attente de validation',
                    url: URL+'listing/operation',
                    type:'ENATTENTE_VALIDATION'

                }

            ];
            vm.itemSearch = {
                "statutOperation":null,
                "canalId": null,
                "typeServiceId": null,
                "montantOp": null,
                "dateOperation": null,
                "uoAcquereurId": null,
                "dateFin": null,
                "codeOperation":null,
                "personneId":null
            };
            function getEtatO(id){
                vm.etat = _.find(vm.etats, {'ident': id});
                vm.item.type=vm.etat.type;
                vm.itemSearch.statutOperation=vm.item.type;
                operationStatut(vm.etat.type);
                vm.ctrlOp=true;
                console.log(id);
            }
            vm.clear = function () {
                vm.etatIdent = null;
                vm.etat = null;
            };
           // operationStatut();

            function operationStatut(type) {
                Operation.queryStatut({statutOperation:type},function (result) {
                    vm.datas=result;
                    init();
                    console.log(result);
                })
            }
            function init() {
                var addItemData = {
                    templateUrl: 'app/entities/operation/operation-dialog.html',
                    controller: 'OperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                montantOp: null,
                                montantFrais: null,
                                dateOperation: null,
                                statut: null,
                                codeRetrait: null,
                                id: null
                            };
                        },
                        isViewing: false,
                        isExpired: false,
                        isMaj:false
                    }
                };

                /**
                 *Modification d'un élément
                 */
                var editItemData = {};

                /**
                 * Suppresion d'un élément
                 */
                var deleteItemData = {};

                var detailItemData = {
                    templateUrl: 'app/entities/operation/envoi_cash/init-operation.html',
                    controller: 'OperationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: null, //valeur déterminé à l'appel de la boite
                        isViewing: true,
                        isExpired: false,
                        isMaj:false
                    }
                };

                var dialogData = {
                    addData: addItemData,
                    editData: editItemData,
                    deleteData: deleteItemData,
                    detailData: detailItemData
                };
                var entitySearchParams = function () {
                    return {};
                };


                var entityParams = function () {
                    console.log(vm.item.statutOperation);
                    return {
                        "statutOperation": vm.itemSearch.statutOperation,
                        "canalId": null,
                        "typeServiceId": null,
                        "montantOp": null,
                        "dateOperation": null,
                        "dateFin": null,
                        "uoAcquereurId": null,
                        "codeOperation": null,
                        "personneId": null,
                        "organisationId": null
                    };
                };
                var datas= {
                    scope: $scope,
                    vm: vm,
                    entity:RequestUtils.OperationOptimise,
                   // entitySearch:Operation,
                   // dialogData: dialogData,
                    entitySearchParams: entitySearchParams,
                    entityParams: entityParams
            };
                TableService.init(datas);
            }

            function genererO() {
                vm.item.type=vm.etat.type;
                vm.item.format=vm.format;
                console.log(vm.item.type);
                if(vm.format===null || vm.format===''){
                    vm.item.format = 'PDF';
                }
                if(vm.item.format ==='PDF' || vm.item.format ===null)
                {
                    Utils.postPrintUrl(vm.etat.url,vm.item);
                } else {
                        if (vm.item.format === 'XLSX') {
                            vm.name = 'ExportEtatatOperation.xlsx';
                        }
                        if (vm.item.format === 'CSV') {
                            vm.name = 'ExportEtatatOperation.csv';
                        }
                    Utils.downloadFile(vm.etat.url,vm.item,vm.name);
                }
            }
        }
    }


)();
