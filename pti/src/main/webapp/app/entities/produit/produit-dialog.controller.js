(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProduitDialogController', ProduitDialogController);

    ProduitDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Produit', 'Ecran','Canal','Facturier','ProduitCanal','TypeService'];

    function ProduitDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Produit, Ecran,Canal,Facturier,ProduitCanal, TypeService) {
        var vm = this;
        vm.produit = entity;
        console.log('----ok ****',vm.produit);
        vm.clear = clear;
        vm.save = save;
        vm.ecrans = Ecran.query();
        vm.facturiers=Facturier.query();
        vm.produitCanal=ProduitCanal.query();
        
        vm.serviceAssocies = TypeService.queryAllOptimise(function (response) {
            
        if(vm.produit.serviceAssocieId){
            angular.forEach(response, function (value, index) {
                console.log('value.id',value.id);
                    if(value.id===vm.produit.serviceAssocieId) {
                        vm.produit.serviceAssocie = value;
                        console.log('produit canal',vm.produit);
                    }
            });
        }
        });

        if(vm.produit.facturierId){
            vm.produit.facturier=Facturier.get({id:vm.produit.facturierId});
            console.log('ffffffact',vm.produit.facturier);
        }

        Canal.query(function (response) {
            console.log('response',response);
            var i = 0;
            if(response){
                vm.canals = [];
                vm.canalList = [];
                angular.forEach(response, function (value, index) {
                    var v = {};
                    v.name = value.codeCanal;
                    i++;
                    v.id = value.id;
                    v.title = value.libelleCanal;
                    vm.canalList.push(v);
                    vm.canals.push(value.libelleCanal);
                });
                console.log('canals',vm.canals);

            }

        });

        if(vm.produit!=null){
            console.log('produit canal',vm.produitCanal);
            vm.canalLibelle=[];
            vm.canalIds=[];
            vm.canalListIds=[];
            console.log('produit canal',vm.produit);
            ProduitCanal.query(function (response) {
                var i = 0;
                if(response){
                    angular.forEach(response, function (value, index) {
                        console.log('1111',value);
                        if(vm.produit.id===value.produitId){
                            vm.canalIds.push(value.canalId);
                            vm.canalLibelle.push(value.canalLibelle);
                            vm.canalListIds.push(value.canalId);
                        }

                    });
                }
                vm.listIdcanal=vm.canalLibelle;
            });



            //console.log('ok',vm.listIdcanal);


        }

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        vm.organisationColums = [{
            title: 'libelle',
            name: 'title'
        }];

        $scope.$watch("vm.canalIds", function (ids) {
            if (angular.isDefined(ids)) {
                vm.canalListIds = [];
                vm.listIdcanal=[];
                console.log('liste ag',vm.canalList);
                angular.forEach(ids, function (value, index) {
                    var test;
                    test = _.find(vm.canalList, {'id': value});
                    console.log(test);
                    if (test) {
                        vm.canalListIds.push(test.id);
                        vm.listIdcanal.push(test.title);
                    }
                });
            }
        });

        function save () {
            console.log('Reuest to save produit');
            vm.isSaving = true;
            vm.produit.canalIds=vm.canalListIds;
            vm.produit.facturierId=vm.produit.facturier.id;
              vm.produit.serviceAssocieId=vm.produit.serviceAssocie.id;
            if (vm.produit.actif===true){
                vm.produit.actif=1;
            }else{
                 vm.produit.actif=0;
            }
            
            if (vm.produit.transactionnel===true){
                vm.produit.transactionnel=1;
            }else{
                 vm.produit.transactionnel=0;
            }
            console.log(vm.produit);
            if (vm.produit.id !== null) {
                  Produit.update(vm.produit, onSaveSuccess, onSaveError);
            } else {
                  Produit.save(vm.produit, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
           $scope.$emit('ptiApp:produitUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
