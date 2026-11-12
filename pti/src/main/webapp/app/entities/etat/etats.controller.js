(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('EtatController', EtatController);

        EtatController.$inject = ['Notification','OperationUtils','BASE_URL_API','$scope','$state','TypeService','RequestUtils','Utils'];

        function EtatController(Notification,OperationUtils,BASE_URL_API,$scope,$state,TypeService,RequestUtils,Utils) {

            var vm = this;
            var URL = BASE_URL_API+"reporting/";
            vm.typeServices = TypeService.queryAll();
            console.log('vm.typeServices',vm.typeServices);
            vm.format = null;
            vm.openCalendar = openCalendar;
            vm.datePickerOpenStatus = {};

            OperationUtils.initVerificationOrganisatioType(vm);

            RequestUtils.GetOrganisationByType.get({ type: "PAYS" }, function(datas) {
                vm.pays = datas;
            });
            
      

            RequestUtils.UserOrganisation.get({}, function(data) {
                console.log("organisation de l'utilisateur");
                vm.userOrganisation=data;
                
                RequestUtils.OrganisationsFilsByType.get({uoId:vm.userOrganisation.id, type: "MASTER" }, function(datas) {
                    vm.masters = datas;
                });
    
                RequestUtils.OrganisationsFilsByType.get({ uoId:vm.userOrganisation.id,type: "SOUS_DISTRIBUTEUR" }, function(datas) {
                    vm.sousDistributeurs = datas;
                });

            });
    
    
            

            function openCalendar(date) {
                console.log("open");
                vm.datePickerOpenStatus[date] = true;
            }

            vm.item = {
                typeServiceId:null,
                uoConnecteId:null,
                uoPaysId:null,
                dateDebut:null,
                dateFin:null,
                format: null
            };

            vm.etats = [{
                id: 1,
                libelle: "Bilan des opérations par service et pays",
                params: {
                    typeService:true,
                    uoPaysId:true
                },
                url: URL + "operations/service-details"
            },{
                id: 2,
                libelle: "Bilan des opérations",
                params: {
                    typeService:true
                },
                url: URL + "operations/service-bilan"
            }/*,{
                id: 3,
                libelle: "Bilan des commissions",
                params: {
                },
                url: URL + "commissions/all"
            }*/,{
                id: 4,
                libelle: "Bilan envois & receptions",
                params: {
                },
                url: URL + "operations/cumuls"
            }];

            vm.getEtatChoisi = function (id) {
                vm.etat = _.find(vm.etats, {'id': id});
                reset();
            };

            function reset() {
                vm.item = {
                    typeServiceId:null,
                    uoConnecteId:null,
                    uoPaysId:null,
                    dateDebut:null,
                    dateFin:null,
                    format: null
                };
            }

            function verif(){
                if(vm.item.dateFin!=null && vm.item.dateDebut==null){
                    Notification.error("Veuillez definir la date de debut!");
                    return false;
                }
                return true;
            }

            vm.printTest = function(){
                Utils.postPrintUrl( URL + "operations/cumuls_periode", {});
            };

            
            vm.save = function () {
                //Date debut et date Fin sont obligatoire
                vm.item.format=vm.format;
                if(verif()) {
                    //Utils.postPrintUrl(vm.etat.url, vm.item);
                    if (vm.format === null || vm.format==='') {
                        vm.item.format = 'PDF';
                    }
                    console.log(vm.item);
                    if (vm.item.format === 'PDF' || vm.format === null) {
                        Utils.postPrintUrl(vm.etat.url, vm.item);
                    } else {
                        if (vm.item.format === 'XLSX') {
                            vm.name = 'ExportOperation.xlsx';
                        }
                        if (vm.item.format === 'CSV') {
                            vm.name = 'ExportOperation.csv';
                        }
                        Utils.downloadFile(vm.etat.url, vm.item, vm.name);
                    }
                }
            };

            vm.clear = function () {
                reset();
                vm.etatId = null;
                vm.etat = null;
            }

        }
    }

)();
