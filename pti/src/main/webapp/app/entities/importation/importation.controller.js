(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ImportationController', ImportationController);

    ImportationController.$inject = ['$state', 'Utilitaire','AlertService'];

    function ImportationController($state, Utilitaire,  AlertService) {

        var vm = this;

        vm.startOrganisation=startOrganisation;
        vm.startOrganisationCompte=startOrganisationCompte;





        function startOrganisation() {
            console.log('1');
            Utilitaire.startOrganisationAPI(function (data) {
                    console.log('data',data);
            })
        }

        function startOrganisationCompte() {
            console.log('2');
            Utilitaire.startOrganisationCompteAPI(function (data) {
                console.log('data',data);
            })
        }


    }
})();
