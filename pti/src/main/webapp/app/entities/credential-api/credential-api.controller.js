(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CredentialApiController', CredentialApiController);

    CredentialApiController.$inject = ['$state', '$scope','CredentialApi', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams','$uibModal'];

    function CredentialApiController($state, $scope,CredentialApi, ParseLinks, AlertService, paginationConstants, pagingParams,$uibModal) {

        var vm = this;

        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.updateOauth=updateOauth;
        vm.canShowResultCount=false;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        // CredentialApi.getOauthClientDet(function (response) {
        //     console.log('---get-----',response);
        //    // vm.credentialApis=response;
        //     vm.totalItems=response.length+1;
        //     vm.canShowResultCount=true;
        // });

         loadAll();
        $scope.$watch("vm.page", function() {
            loadAll();
        });

        function loadAll () {
            console.log("***Loading items");
            vm.canShowResultCount=true;
            console.log(vm.itemsPerPage,pagingParams.page);
            CredentialApi.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'client_id') {
                    result.push('client_id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.credentialApis = data;
                vm.page = pagingParams.page;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }
        function updateOauth(id) {
            CredentialApi.getOneOauth({client_id:id},function (result) {
                vm.credentialApis=result;
            });
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
