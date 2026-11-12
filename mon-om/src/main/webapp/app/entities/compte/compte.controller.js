(function () {
    'use strict';

    angular
            .module('omnicanalApp')
            .controller('CompteController', CompteController);

    CompteController.$inject = ['$state', 'Agence', 'ParseLinks', 'AlertService', 'paginationConstants', '$stateParams', 'Compte', 'pagingParams', 'FournisseurService', 'TypeCompte','TypeService'];

    function CompteController($state, Agence, ParseLinks, AlertService, paginationConstants, $stateParams, Compte, pagingParams, FournisseurService, TypeCompte, TypeService) {

        var vm = this;
        vm.affiche = affiche;

        vm.teneur = 0;
        vm.titulaire = 0;
        vm.type = 0;
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.comptes = [];
        vm.fournisseurServices = FournisseurService.getAllFse();
        vm.typecomptes = TypeCompte.query();
         vm.typeServices = TypeService.query();

        if (angular.isDefined($stateParams.idrelation) && $stateParams.idrelation !== "") {
            loadAllRelation();
            vm.rel = 1;
        } else {
            vm.rel = 0;
            loadAll();
        }
        function loadAllRelation() {
            Compte.queryCompte({idrelation: $stateParams.idrelation}, function (result) {
                vm.comptes = result;
                vm.searchQuery = null;
            });
        }

        function affiche() {
            vm.teneur = 0;
            vm.titulaire = 0;
            vm.type = 0;
            if (angular.isDefined(vm.fseTeneur) && vm.fseTeneur !== null)
                vm.teneur = vm.fseTeneur.id;
            if (angular.isDefined(vm.fseTitulaire) && vm.fseTitulaire !== null)
                vm.titulaire = vm.fseTitulaire.id;
            if (angular.isDefined(vm.typeCompte) && vm.typeCompte !== null)
                vm.type = vm.typeCompte.id;
            pagingParams.type = vm.type;
            pagingParams.titulaire = vm.titulaire;
            pagingParams.teneur = vm.teneur;
            if (angular.isDefined($stateParams.idrelation) && $stateParams.idrelation !== "") {
                loadAllRelation();
                vm.rel = 1;
            } else {
                vm.rel = 0;
                loadAll();
            }

        }

        function loadAll() {
            console.log(pagingParams);
            Compte.queryCompteTeneur({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort(),
                teneur: pagingParams.teneur,
                titulaire: pagingParams.titulaire,
                type: pagingParams.type
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
                vm.comptes = data;
                vm.page = pagingParams.page;
                vm.typeCompte = {id: pagingParams.type};
                vm.fseTitulaire = {id: pagingParams.titulaire};
                vm.fseTeneur = {id: pagingParams.teneur};
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
                search: vm.currentSearch,
                titulaire: pagingParams.titulaire,
                teneur: pagingParams.teneur,
                type: pagingParams.type
            });

        }
    }
})();
