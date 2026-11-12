(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('InterfaceUtilisateurController', InterfaceUtilisateurController);

    InterfaceUtilisateurController.$inject = ['InterfaceUtilisateur','$state', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams','FournisseurService'];

    function InterfaceUtilisateurController(InterfaceUtilisateur,$state, ParseLinks, AlertService, paginationConstants, pagingParams, FournisseurService) {

        var vm = this;
        vm.interfaceUtilisateurs = [];
        vm.fournisseurServices = FournisseurService.getAllFse();
        vm.affiche = affiche;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.loadPage = loadPage;

        loadAll();

        function affiche() {
            vm.pfse = null;
            vm.ptypeMethode = null;

            if (angular.isDefined(vm.fse) && vm.fse !== null)
                vm.pfse = vm.fse.codeFse;
            if (angular.isDefined(vm.typeMethode) && vm.typeMethode !== null)
                vm.ptypeMethode = vm.typeMethode;

            InterfaceUtilisateur.interfaceUserFiltered({
                    pfse : vm.pfse,
                    ptypeMethode : vm.ptypeMethode
                }, function (data) {
                    vm.interfaceUtilisateurs = data;
                    console.log(vm.interfaceUtilisateurs);
                },
                function () {}
            );
        }

        function loadAll() {
            // InterfaceUtilisateur.query(function(result) {
            //     vm.interfaceUtilisateurs = result;
            //     vm.searchQuery = null;
            // });
            InterfaceUtilisateur.query({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'codeInterface') {
                    result.push('codeInterface');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.interfaceUtilisateurs = data;
                vm.page = pagingParams.page;
                vm.apiId = pagingParams.apiId;
                vm.searchQuery = null;
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
