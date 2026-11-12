(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('BwCustomerController', BwCustomerController);

    BwCustomerController.$inject = ['BwAccount', 'AppService', 'BwCustomer', '$state', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function BwCustomerController(BwAccount, AppService, BwCustomer, $state, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.currentSearc = {};
        vm.activetab = "encours";
        vm.encours = 0;
        vm.rechercher = rechercher;
        vm.bwCustomers = [];
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.activerTab = activerTab;
        vm.openModal = openModal;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        loadAll();
        function rechercher() {
            loadAll(vm.nom, vm.prenom, vm.telephone,   vm.encours);
            vm.currentSearc = {nom: vm.nom, prenom: vm.prenom, telephone: vm.telephone};

        }


        function openModal(bwCustomers) {
            //    $scope.$emit('appliApessApp:pocheDepenseUpdate',idPoche);
            var modalNew = {
                templateUrl: 'app/entities/bw-account/bw-account-dialog.html',
                controller: 'BwAccountDialogController',
                controllerAs: 'vm',
                size: 'lg',
                resolve: {
                    entity: ['BwAccount', function (BwAccount) {
                            return BwAccount.queryAccountCus({cusId: bwCustomers.id}).$promise;
                        }]
                }
            };
            AppService.showModal(modalNew);
        }


        function activerTab(tab, valide) {
            vm.activetab = tab;
           vm.encours = valide;
             rechercher();
        }
        function verifvar(vr) {
            var vresult = "0";
            if ((angular.isDefined(vr) && vr !== "")) {
                vresult = vr;
            }
            return vresult;
        }
        function loadAll(pnom, pprenom, ptelephone) {

            BwCustomer.queryCust({
                page: pagingParams.page - 1,
                size: vm.itemsPerPage,
                sort: sort(), nom: verifvar(pnom), prenom: verifvar(pprenom), telephone: verifvar(ptelephone), valide:vm.encours
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
                vm.bwCustomers = data;

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

        function transition(pnom, pprenom, ptelephone) {

            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')
                , nom: verifvar(vm.currentSearc.nom), prenom: verifvar(vm.currentSearc.prenom), telephone: verifvar(vm.currentSearc.telephone),
                search: vm.currentSearc
            }, function (data) {
                console.log(data);
                vm.nom = pnom;
                vm.prenom = pprenom;
                vm.telephone = ptelephone;
            });

        }




    }
})();
