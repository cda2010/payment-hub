(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteHistoriqueController', CompteHistoriqueController);

    CompteHistoriqueController.$inject = ['RequestUtils','$rootScope','Compte', '$scope','TransactionComptePti', 'BASE_URL_API','Utils'];

    function CompteHistoriqueController(RequestUtils,$rootScope, Compte, $scope,TransactionComptePti, BASE_URL_API,Utils) {
        var vm = this;
        vm.valeurCompte=null;
        vm.chercherCompte=chercherCompte;
        vm.compte=null;
        vm.dateDebut=null;
        vm.dateFin=null;
        vm.transactions=[];
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.onSearch=false;
        vm.imprimer= imprimer;
        var URL = BASE_URL_API+"etat/compte-historique";

        function chercherCompte(valeurCompte) {
            console.log(vm.dateDebut,valeurCompte);
             Compte.queryByValeurCompte({valeurCompte:valeurCompte},function (result) {
                vm.compte=result;
                console.log("----------------------------------------------------------------",result);
                 vm.onSearch=true;
                 if(vm.compte.id){
                    TransactionComptePti.findListTransaction({compteAutreId:vm.compte.id,dateDebut:vm.dateDebut,dateFin:vm.dateFin},function (result) {
                    vm.transactions=result;
                        console.log(vm.transactions);
                    })
                 }else {
                    vm.transactions=[];
                 }
            })
        }
        vm.datePickerOpenStatus.dateHeureEnvoie = false;
        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }

        vm.item = {
            format: null,
            dateDebut:null,
            dateFin:null,
            numeroCompte:null,
            data:null,
        }

        function imprimer() {
            console.log('format',vm.format);
            vm.item.data = vm.transactions
            vm.item.numeroCompte = vm.valeurCompte
            vm.item.dateDebut = vm.dateDebut
            vm.item.dateFin = vm.dateFin
            if(vm.format===null || vm.format==='' || vm.format===undefined){
                console.log('f',vm.format);
                vm.item.format = 'PDF';
            }else {
                vm.item.format=vm.format;
            }
            if(vm.item.format ==='PDF' || vm.item.format ===null)
            {
                Utils.postPrintUrl(URL,vm.item);
            } else
                {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'HistoriqueCompte.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'HistoriqueCompte.csv';
                    }
                Utils.downloadFile(URL,vm.item,vm.name);
            }

        }

    }

})();
