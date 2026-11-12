(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('EtatCompteController', EtatCompteController);

        EtatCompteController.$inject = ['Notification','OperationUtils','BASE_URL_API','$scope','$state','TypeService','RequestUtils','Utils'];

        function EtatCompteController(Notification,OperationUtils,BASE_URL_API,$scope,$state,TypeService,RequestUtils,Utils) {

            var vm = this;
            var URL = BASE_URL_API+"etat/";
            vm.format = null;
            vm.openCalendar = openCalendar;
            vm.datePickerOpenStatus = {};
            vm.dateCtrl=false;
            vm.name=null;




            function openCalendar(date) {
                console.log("open");
                vm.datePickerOpenStatus[date] = true;
            }

            vm.item = {
                typeClient:null,
                uoConnecteId:null,
                uoPaysId:null,
                dateDebut:null,
                dateFin:null,
                format: null,
                libelle: null,
                statutCompte: null,
                typeAgent: null,
                typeEvaluation: null
            };

            vm.etats = [{
                id: 1,
                libelle: "Bilan des comptes clients",
                params: {
                    typeClient:true,
                    uoPaysId:true,
                    dateCtrl: true
                },
                url: URL + "etat-compte/client"
            },
                /*{
                    id: 2,
                    libelle: "Total comptes de monnaie electronique",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true
                    },
                    url: URL + "etat-compte/periode"
                },*/
                {
                    id: 2,
                    libelle: "Comptes de monnaie electronique clients actifs 3 derniers mois",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true
                    },
                    url: URL + "etat-compte/actif-in-3-months"
                },
                {
                    id: 3,
                    libelle: "Comptes de monnaie electronique agents actifs 3 derniers mois",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true,
                        typeAgent: true,
                    },
                    url: URL + "etat-compte/actif-agent-in-3-months"
                },
                {
                    id: 4,
                    libelle: "Total titulaires de Comptes de monnaie electronique",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true
                    },
                    url: URL + "etat-compte/titulaires"
                },
                {
                    id: 5,
                    libelle: "Total adultes titulaires de Comptes de monnaie electronique",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true
                    },
                    url: URL + "etat-compte/adultes"
                },
                {
                    id: 6,
                    libelle: "Total femmes titulaires de Comptes de monnaie electronique",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true,
                        libelle: "FEMININ"
                    },
                    url: URL + "etat-compte/femmes"
                },
                {
                    id: 7,
                    libelle: "Total femmes adultes titulaires de Comptes de monnaie electronique",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true,
                        libelle: "FEMININ"
                    },
                    url: URL + "etat-compte/femmes-adultes"
                },
                {
                    id: 8,
                    libelle: "Total hommes titulaires de Comptes de monnaie electronique",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true,
                        libelle: "MASCULIN"
                    },
                    url: URL + "etat-compte/femmes"
                },
                {
                    id: 9,
                    libelle: "Total hommes adultes titulaires de Comptes de monnaie electronique",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true,
                        libelle: "FEMININ"
                    },
                    url: URL + "etat-compte/femmes-adultes"
                },
                {
                    id: 10,
                    libelle: "Total de bénéficiaires de transfert non titulaires de porte-monnaie électronique",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: false
                    },
                    url: URL + "export/liste-beneficiaires-transfert-non-titulaires-de-porte-monnaie-électronique"
                },
                {
                    id: 11,
                    libelle: "Liste des sous-distributeurs",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true
                    },
                    url: URL + "export/liste-sous-distribibuteurs"
                },
                {
                    id: 12,
                    libelle: "Liste comptes banquaires",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true,
                        statutCompte: true
                    },
                    url: URL + "export/liste-banque-actif"
                },
                {
                    id: 13,
                    libelle: "Liste des points de ventes actifs",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true
                    },
                    url: URL + "export/liste-points-ventes-actifs"
                },

                {
                    id: 14,
                    libelle: "agents de validations",
                    params: {
                        typeClient:false,
                        uoPaysId:false,
                        dateCtrl: true,
                        typeEvaluation: true,
                    },
                    url: URL + "etat-compte/agent-validation"
                },



            ];

            vm.getEtatChoisi = function (id) {
                vm.etat = _.find(vm.etats, {'id': id});
                reset();
            };
            console.log('vm.etats',vm.etats);
            function reset() {
                vm.item = {
                    typeClient:null,
                    uoConnecteId:null,
                    uoPaysId:null,
                    dateDebut:null,
                    dateFin:null,
                    format: null,
                    statut: null,
                    libelle: null,
                    statutCompte: null,
                    typeAgent: null,
                    typeEvaluation:null
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
                console.log('vm.etat',vm.etat);
                if(vm.etat.id===6|| vm.etat.id===7){
                    vm.item.libelle="FEMININ";
                }
                if(vm.etat.id===8||vm.etat.id===9){
                    vm.item.libelle="MASCULIN";
                }
                vm.item.format=vm.format;
                vm.item.statut='ETAT';
                if(vm.item.statutCompte!==null){
                    vm.item.statut=vm.item.statutCompte;
                }
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
                            vm.name = 'etatCompteClient.xlsx';
                        }
                        if (vm.item.format === 'CSV') {
                            vm.name = 'etatCompteClient.csv';
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
