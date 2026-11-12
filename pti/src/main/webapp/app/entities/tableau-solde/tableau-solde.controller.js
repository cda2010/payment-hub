(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('TableauSoldeController', TableauSoldeController);

        TableauSoldeController.$inject = ['Notification','OperationUtils','BASE_URL_API','$scope','$state','RequestUtils','Utils','TableauSolde','UniteOrganisation', 'TypeUniteOrganisation'];

        function TableauSoldeController(Notification,OperationUtils,BASE_URL_API,$scope,$state,RequestUtils,Utils,TableauSolde, UniteOrganisation, TypeUniteOrganisation) {

            var vm = this;
            var URL = BASE_URL_API+"etat/export/solde";
            vm.req=null;
            vm.tabAffiche=false;
            vm.formatCtl=false;
            vm.afficherSolde= afficherSolde;
            vm.soldeCancel=soldeCancel;
            vm.genererAll=genererAll;
            vm.typeuniteorganisations =[] ;
            vm.marchand1='MARCHAND';
            vm.agent='MASTER';
            vm.agence='AGENCE';
            vm.com='COM';
            vm.format=null;
            vm.comatt='COMATT';
            vm.devise='FCFA';
            vm.item = {
                pereUoId: null,
                typeUO: null
            };
            vm.FSEs=UniteOrganisation.queryByTypeUoFSE();
            TypeUniteOrganisation.query(function (typeUniteOrganisations) {
                let i=0;
                for(i=0;i<typeUniteOrganisations.length;i++){
                    if(typeUniteOrganisations[i].libelleTypeUO !="PAYS"
                        &&typeUniteOrganisations[i].libelleTypeUO !="FSE"
                        &&typeUniteOrganisations[i].libelleTypeUO !="ZONE"){
                        vm.typeuniteorganisations.push(typeUniteOrganisations[i]);
                    }
                }
            });
            function getDevise(){
                TableauSolde.getDevise({pereUoId: vm.item.pereUoId},function (result) {
                    console.log(result.valeur);
                    if(result.valeur!=null){
                        vm.devise=result.valeur;
                    }
                    console.log(vm.devise)
                });
            }
            function soldeCancel() {
                vm.item = {
                    pereUoId: null,
                    typeUO: null,
                    format: null
                };
                vm.tabAffiche=false;
                vm.formatCtl=false;
            }

            function afficherSolde() {
                vm.formatCtl=true;
                console.log('code fse',vm.item.pereUoId);
                vm.tabAffiche=true;
                vm.item.typeUO= 'AGENCE';
                getDevise();
                TableauSolde.getAllSolde({pereUoId: vm.item.pereUoId},function (result) {
                    console.log(result);
                    vm.principal=result;
                    console.log(vm.principal.distribution)
                });
            }

            function genererAll() {
                vm.dateNow = new Date();
                console.log('my date',vm.dateNow);
                vm.item.format = vm.format;
                if(vm.format===null || vm.format===''){
                    console.log('f',vm.format);
                    vm.item.format = 'PDF';
                }
                if(vm.item.format ==='PDF' || vm.item.format ===null)
                {
                    Utils.postPrintUrl(URL,vm.item);
                } else {
                    if(vm.item.format ==='XLSX') {vm.name='Exportsolde.xlsx';}
                    if(vm.item.format ==='CSV') {vm.name='Exportsolde.csv';}
                    Utils.downloadFile(URL,vm.item,vm.name);
                }
            }

        }
    }

)();
