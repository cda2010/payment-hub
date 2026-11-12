(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneReglementModalController', LigneReglementModalController);


    LigneReglementModalController.$inject = ['$scope','$stateParams','$state','$uibModal','$uibModalInstance','pagingParams','Notification','entity','ReglementOrganisation','CancelAlert','ParseLinks','AlertService','paginationConstants'];

    function LigneReglementModalController($scope,$state,$stateParams,$uibModal,$uibModalInstance,pagingParams,Notification,entity,ReglementOrganisation,CancelAlert,ParseLinks, AlertService,paginationConstants) {

        var vm = this;
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.canShowResultCount=false;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.reglementId=entity;
        vm.confirmDetachement = confirmDetachement;
        vm.page=1;
        console.log('yes',vm.reglementId);
        vm.cpt=[];
        $scope.fermerM= function () {
            $uibModalInstance.dismiss('cancel');
        };
        loadAll();

        $scope.$watch("vm.page", function() {
            loadAll();
        });
        function loadAll () {
            console.log("***Loading items");
            vm.canShowResultCount=true;
            console.log(vm.itemsPerPage,pagingParams.page);
            ReglementOrganisation.getLigneReglement({
                reglementId:vm.reglementId,
                page: vm.page - 1,
                size: vm.itemsPerPage,
            }, onSuccess, onError);

            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = data.length;
                vm.ligneReglement = data;
                //vm.page = pagingParams.page;
                console.log('get',data);
            }
            function onError(error) {
                console.log('eereur',error);
                AlertService.error(error.data.message);
            }
        }
        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        function transition() {
            console.log('page',vm.page);
            console.log('state',$state);
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }
        function confirmDetachement(ligneReglement) {
            CancelAlert.confirm(function () {
                ReglementOrganisation.detacherReglement({ligneReglementId:ligneReglement.id},function (result) {
                    console.log(result);
                    Notification.success('Opération éffectuée avec succès');
                    $uibModalInstance.close();
                    $state.reload('reglement-organisation');
                });
            })

        }

        function detacherReglement(ligneReglementId) {
            ReglementOrganisation.detacherReglement({ligneReglementId:ligneReglementId},function (data) {
                console.log(result);
                Notification.success('Opération éffectuée avec succès');
            });
        }


    }
})();
