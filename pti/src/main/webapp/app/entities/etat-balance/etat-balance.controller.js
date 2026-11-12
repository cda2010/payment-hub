(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('EtatBalanceController', EtatBalanceController);

        EtatBalanceController.$inject = ['BASE_URL_API', '$scope', '$state',  'RequestUtils', 'Utils','UniteOrganisation'];

        function EtatBalanceController( BASE_URL_API, $scope, $state, RequestUtils, Utils,UniteOrganisation) {

            var vm = this;
            var URL = BASE_URL_API + "etat/";
            vm.etatIdent=null;
            vm.getEtat = getEtat;
            vm.generer=generer;
            vm.etatFSE=null;
            vm.etatCtrl=false;
            vm.format= null;
            vm.FSEs=UniteOrganisation.queryByTypeUoFSE();
            vm.item={
                type:null,
                fse:null,
                format: null
            };
            vm.etats = [
                {
                    ident:1,
                    libelle:'Balance des soldes comptes clients',
                    url: URL+'balance_cpt/client',
                    type:'CLIENT',
                    params: {
                        fse:false
                    }

                },
                {
                    ident:2,
                    libelle:'Balance des soldes comptes master',
                    url: URL+'balance_cpt/etat',
                    type:'MASTER',
                    params: {
                        fse:true
                    }
                },
                {
                    ident:3,
                    libelle:'Balance des soldes comptes agents ',
                    url: URL+'balance_cpt/etat',
                    type:'SOUS_DISTRIBUTEUR',
                    params: {
                        fse:true
                    }
                },
                {
                    ident:4,
                    libelle:'Balance des soldes comptes marchands',
                    url: URL+'balance_cpt/etat',
                    type:'MARCHAND',
                    params: {
                        fse:true
                    }
                },
                {
                    ident:5,
                    libelle:'Balance des soldes comptes distributeurs CBI (agences)',
                    url: URL+'balance_cpt/etat',
                    type:'AGENCE',
                    params: {
                        fse:true
                    }
                }

            ];

             function getEtat(id){
                 vm.etat = _.find(vm.etats, {'ident': id});
                 console.log(id);
                 console.log(vm.etat.params)
                    }
            vm.clear = function () {
                vm.etatIdent = null;
                vm.etat = null;
                vm.format= null;
            }
            
            function generer() {
                console.log(vm.etat.url);
                vm.item.type=vm.etat.type;
                vm.item.fse=vm.etatFSE;
                vm.item.format = vm.format;
                console.log(vm.item.fse);
                if (vm.format === null || vm.format==='') {
                    vm.item.format = 'PDF';
                }
                if (vm.item.format === 'PDF') {
                    Utils.postPrintUrl(vm.etat.url, vm.item);
                } else {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'ExportBalanceComptes.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'ExportBalanceComptes.csv';
                    }
                    Utils.downloadFile(vm.etat.url, vm.item, vm.name);
                }
                // Balance.etat().success(function (response) {
                //         var file = new Blob([response], {type: 'application/pdf'});
                //         var fileURL = URL.createObjectURL(file);
                //     });

            }
        }
    }


)();
