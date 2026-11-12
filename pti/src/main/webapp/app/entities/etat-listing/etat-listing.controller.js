(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('EtatListingController', EtatListingController);

        EtatListingController.$inject = ['BASE_URL_API', '$scope', '$state',  'TypeUniteOrganisation','UniteOrganisation','RequestUtils', 'Utils'];

        function EtatListingController( BASE_URL_API, $scope, $state,TypeUniteOrganisation,UniteOrganisation ,RequestUtils, Utils) {

            var vm = this;
            var URL = BASE_URL_API + "etat/";
            vm.etatIdent=null;
            vm.etatClient=null;
            vm.typeuniteorganisations =[] ;
            vm.FSEs=UniteOrganisation.queryByTypeUoFSE();
            vm.getEtatL = getEtatL;
            vm.genererL=genererL;
            vm.selectTypeUO=selectTypeUO;
            vm.selectClient=selectClient;
            vm.UO=false;
            vm.format = null;
            vm.openCalendar = openCalendar;
            vm.datePickerOpenStatus = {};
            vm.item={
                type:null,
                format: null,
                pereUoId:null,
                typeUO:null,
                dateDebut:null,
                dateFin:null
            };
            vm.etats = [
                {
                    ident:1,
                    libelle:'Liste des clients',
                    url: URL+'listing/client',
                    type:'CLIENT',
                    params: {
                        type:true
                    }

                },
                {
                    ident:2,
                    libelle:'Liste des organisations',
                    url: URL+'listing/unite-date',
                    type:'ORGANISATION',
                    params: {
                        fse:true,
                        typeUO:true
                    }

                }

            ];
            function openCalendar(date) {
                console.log("open");
                vm.datePickerOpenStatus[date] = true;
            }

            TypeUniteOrganisation.query(function (typeUniteOrganisations) {
                console.log('og',typeUniteOrganisations);
                let i=0;
                for(i=0;i<typeUniteOrganisations.length;i++){
                    if(typeUniteOrganisations[i].libelleTypeUO !="PAYS"
                        &&typeUniteOrganisations[i].libelleTypeUO !="FSE"
                        &&typeUniteOrganisations[i].libelleTypeUO !="ZONE"){
                        vm.typeuniteorganisations.push(typeUniteOrganisations[i]);
                    }
                }
            });

            function getEtatL(id){
                vm.etat = _.find(vm.etats, {'ident': id});
                console.log(id);
                vm.UO=false;
            }

            function selectTypeUO(){
                vm.UO=true;
            }

            function selectClient(){
                vm.UO=true;
            }

            vm.clear = function () {
                vm.etatIdent = null;
                vm.etat = null;
                vm.etatClient=null;
            };

            function genererL() {
                console.log(vm.etat.url,vm.item);
                vm.item.type=vm.etatClient;
                vm.item.format = vm.format;
                if (vm.format === null || vm.format==='') {
                    vm.item.format = 'PDF';
                }
                if (vm.item.format === 'PDF') {
                    Utils.postPrintUrl(vm.etat.url, vm.item);
                } else {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'ExportListeClients.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'ExportListeClients.csv';
                    }
                    Utils.downloadFile(vm.etat.url, vm.item, vm.name);
                }
            }
        }
    }


)();
