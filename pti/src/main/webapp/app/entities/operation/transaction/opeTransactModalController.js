(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OpeTransactModalController', OpeTransactModalController);


    OpeTransactModalController.$inject = ['$scope','$stateParams','$state','$uibModal','$uibModalInstance','pagingParams','Notification','entity','Operation','ParseLinks','AlertService','paginationConstants'];

    function OpeTransactModalController($scope,$state,$stateParams,$uibModal,$uibModalInstance,pagingParams,Notification,entity,Operation,ParseLinks, AlertService,paginationConstants) {

        var vm = this;
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.canShowResultCount=false;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.operationId=entity;
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
            Operation.findTransactionOptimise({
                operationId:vm.operationId,
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
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.transaction = data;
                vm.page = pagingParams.page;
                console.log('transa',data);
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
