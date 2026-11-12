(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailsLotController', DetailsLotController);

        DetailsLotController.$inject = ['$state','BASE_URL_API','Lot', 'DetailsLot', 'UniteOrganisation','pagingParams','ParseLinks','paginationConstants','$uibModal','Utils','Notification'];

    function DetailsLotController($state,BASE_URL_API,Lot,DetailsLot,UniteOrganisation,pagingParams,ParseLinks,paginationConstants,$uibModal,Utils,Notification) {

        var vm = this;
        // var URL = BASE_URL_API + "etat/listing/lot";
        vm.detailsLot = [];
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        // vm.viewLot=viewLot;
        // vm.setActive=setActive;

        // //attributs de filtrage
        // vm.itemSearch = {
        //     'nomFacturier':null,
        //     'codeFacturier':null,
        //     'classification_id':null,
        //     'typeFacturier':null,
        //     'possesseur_id':null,
        //     'format': null
        // };

        // vm.document= {
        //     'valeur':null,
        //     'valeurContentType': null
        // };


        // vm.allData=[];
        // vm.afficheFiltre=false;
        // vm.afficherFiltre=afficherFiltre;
        // vm.chercher=chercher;


        //valeurs de selections
        // vm.typeFacturiers=['','FSE', 'MARCHAND', 'AUTRES'];
        // vm.classifications = [];
        // vm.possesseurs=UniteOrganisation.queryByTypeUoFSE();

        // function initClassifications(idPossesseur){
        //     vm.classifications = ClassificationFacturier.queryFinalByUo({id:idPossesseur})
        // }

        // function chercher(){
        //   //  console.log(vm.itemSearch)
        //     vm.fact= true;
        //     vm.facturiers=filtrer();
        // }

        // function setActive(facturier,estActif) {
        //     facturier.activated=estActif;
        //     var info;
        //     if(estActif){
        //         info= 'Activé';
        //     } else {
        //         info= 'Desactivé';
        //     }
        //     Facturier.update(facturier,function (result) {
        //         console.log('updated',result);
        //         if(info==='Activé'){
        //             Notification.success('Facturier'+ ' '+ facturier.nomFacturier+' '+info);

        //         }else {
        //             Notification.warning('Facturier'+ ' '+ facturier.nomFacturier+' '+info);

        //         }
        //     }, function (result) {
        //         console.log('echec',result);
        //         Notification.error('Echec de l\'activation');
        //         facturier.activated=!facturier.activated;
        //     })
        // }

        // function genererFacturier() {
        //     console.log('data',vm.format);
        //     vm.itemSearch.format=vm.format;
        //     if(vm.format===null || vm.format===''){
        //         console.log('f',vm.format);
        //         vm.itemSearch.format = 'PDF';
        //     }
        //     console.log('top',vm.itemSearch);
        //     if(vm.itemSearch.format ==='PDF' || vm.format ===null)
        //     {
        //         Utils.postPrintUrl(URL,vm.itemSearch);
        //     } else {
        //         if(vm.itemSearch.format ==='XLSX') {vm.name='ExportFacturier.xlsx';}
        //         if(vm.itemSearch.format ==='CSV') {vm.name='ExportFacturier.csv';}
        //         Utils.downloadFile(URL,vm.itemSearch,vm.name);
        //     }
        // }

        // function  filtrer() {
        //     var resultat=[];
        //     let i=0;
        //     for(i=0;i<vm.allData.length;i++){
        //         if(!vm.allData[i].codeFacturier){
        //             vm.allData[i].codeFacturier=" ";
        //         }
        //         if(!vm.allData[i].nomFacturier){
        //             vm.allData[i].nomFacturier=" ";
        //         }
        //         if((! vm.itemSearch.possesseur_id ||vm.allData[i].possesseur.id==vm.itemSearch.possesseur_id) &&
        //             (!vm.itemSearch.classification_id||vm.allData[i].classification.id==vm.itemSearch.classification_id) &&
        //             (!vm.itemSearch.typeFacturier||vm.allData[i].typeFacturier==vm.itemSearch.typeFacturier)&&
        //             (! vm.itemSearch.codeFacturier||vm.allData[i].codeFacturier.includes(vm.itemSearch.codeFacturier))&&
        //             (! vm.itemSearch.nomFacturier||vm.allData[i].nomFacturier.includes(vm.itemSearch.nomFacturier))
        //         ){
        //             resultat.push(vm.allData[i]);
        //         }
        //     }
        //     return resultat;
        // }

        // function afficherFiltre(){
        //     vm.afficheFiltre=!vm.afficheFiltre;
        //     if(vm.allData.length<=0){
        //         vm.allData=Facturier.query();
        //     }
        //     if (!vm.afficheFiltre) {
        //         vm.facturiers =Facturier.query();
        //         vm.itemSearch = {
        //             'nomFacturier':null,
        //             'codeFacturier':null,
        //             'classification_id':null,
        //             'typeFacturier':null,
        //             'possesseur_id':null,
        //             'format': null,
        //         }
        //     }
        // }


        // function viewLot(idLot) {
        //     $uibModal.open({
        //         templateUrl: 'app/entities/lot/lot-detail.html',
        //         // controller: 'LotDetailController',
        //         controllerAs: 'vm',
        //         backdrop: 'static',
        //         size: 'lg',
        //         resolve: {
        //             entity: ['Lot', function(Lot) {
        //                 return Lot.get({id : idLot}).$promise;
        //             }]
        //         }
        //     }).result.then(function() {
        //         //$state.go('facturier', null, { reload: 'facturier' });
        //     }, function() {
        //         //$state.go('^');
        //     });

        // }

        loadAll();

        function loadAll() {
            DetailsLot.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);


            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }

            function onSuccess(data, headers) {
                vm.detailsLot = data;
                console.log(vm.detailsLot);
                // vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.page = pagingParams.page;
            }

            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        function transition() {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }

    }
})();
