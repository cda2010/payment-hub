(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['$interval','$uibModal','$scope', 'Principal', 'LoginService', '$state', 'RequestUtils', 'Operation','Notification'];

    function HomeController($interval, $uibModal, $scope, Principal, LoginService, $state, RequestUtils, Operation,Notification) {
        var vm = this;

        InitiateEasyPieChart.init();
        InitiateSparklineCharts.init();
        vm.account = null;
        vm.isAuthenticated = null;
        vm.login = LoginService.open;
        vm.statutOperation = null;
        vm.statuts = ["PAYE", "ENATTENTE", "ENATTENTE_VALIDATION", "ANNULLE", "PAYE", "SUSPENDU", "REJETE", "VALIDE", 'REMBOURSE'];
        //vm.register = register;
        vm.loadImages=loadImages;
        vm.spinnerLoad=spinnerLoad;
        vm.startDashBoard=startDashBoard;
        vm.tableau=false;

        vm.dashB=false;
        $scope.loader = false;
        $scope.images = false;
        vm.options = {
            legend: {
                display: true,
                position: 'left'
            }
        };
        vm.labelsPeriode=[];
        //vm.dataPeriode=[65, 59, 80, 81, 56, 55, 40, 23, 64, 51, 38, 26];
        vm.initCanvasData=initCanvasData;
        vm.periode={
            value:'today',
            displayName:'Aujourd\'hui',
            group:'---Par heure---'
            };
        vm.periodes=[
            {
                value:'today',
                displayName:'Aujourd\'hui',
                group:'---Par heure---'
            },
            {
                value:'yesterday',
                displayName:'Hier',
                group:'---Par heure---'
            },
            {
                value:'7days',
                displayName:'Les 7 derniers jours',
                group:'---Par jour---'
            },
            {
                value:'14days',
                displayName:'Les 14 derniers jours',
                group:'---Par jour---'
            },
            {
                value:'30days',
                displayName:'Les 30 derniers jours',
                group:'---Par jour---'
            },

            {
                value:'thisweek',
                displayName:'Cette semaine',
                group:'---Par jour---'
            },
            {
                value:'lastweek',
                displayName:'La semaine dernière',
                group:'---Par jour---'
            },
            {
                value:'thismonth',
                displayName:'Ce mois',
                group:'---Par jour---'
            },
            {
                value:'lastmonth',
                displayName:'Le mois dernier',
                group:'---Par jour---'
            },
            {
                value:'12month',
                displayName:'Les 12 derniers mois',
                group:'---Par mois---'
            },
            {
                value:'thisyear',
                displayName:'Cette année',
                group:'---Par mois---'
            },
            {
                value:'lastyear',
                displayName:'L\'année dernière',
                group:'---Par mois---'
            },
            {
                value:'5year',
                displayName:'Les 5 dernières années',
                group:'---Par année---'
            },
        ];
        function initCanvasData(){
            vm.labelsPeriode=[];
            vm.dataPeriode=[];
            console.log('1.2 periode',vm.dataPeriode);
            if(vm.periode.group=='---Par heure---'){
                    Operation.queryByHour({periode:vm.periode.value},function (result) {
                        var i;
                        for (i=0;i<24;i++){
                            vm.labelsPeriode.push(result[i].label);
                            vm.dataPeriode.push(result[i].value);
                        }
                    },function (error) {
                    Notification.warning("Opération échouée");
                    })
            }else if(vm.periode.group=='---Par jour---') {
                console.log('Par jour');
                //vm.labelsPeriode=["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];
                Operation.queryByDay({periode:vm.periode.value},function (result) {
                    var i;
                    console.log('Par result',result);
                    for (i=0;i<result.length;i++){
                        vm.labelsPeriode.push(result[i].label);
                        vm.dataPeriode.push(result[i].value);
                    }
                },function (error) {
                    Notification.warning("Opération échouée");
                })
            }else if(vm.periode.group=='---Par mois---') {
                //vm.labelsPeriode=["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];
                Operation.queryByMonth({periode:vm.periode.value},function (result) {
                    var i;
                    for (i=0;i<result.length;i++){
                        vm.labelsPeriode.push(result[i].label);
                        vm.dataPeriode.push(result[i].value);
                    }
                },function (error) {
                    Notification.warning("Opération échouée");
                })
            }else{
                Operation.queryByYear(function (result) {
                    var i;
                    for (i=0;i<result.length;i++){
                        vm.labelsPeriode.push(result[i].label);
                        vm.dataPeriode.push(result[i].value);
                    }
                },function (error) {
                    Notification.warning("Opération échouée");
                })
            }
            console.log('canvas',vm.labelsPeriode,vm.dataPeriode)

        }
        //initCanvasData();
        vm.traitementOperation = function (operation) {
            console.log('ol');
            $uibModal.open({
                templateUrl: 'app/entities/operation/account-cash/account-cash-traitement.html',
                controller: 'OperationTraitementController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return operation;
                    }
                }
            }).result.then(function () {
                //$uibModalInstance.close(true);
            });
        };

        $interval(function () {
            if ($state.current.name === 'home') {
               init();
            }
        }, 100000);
       


        /* RequestUtils.BilanMensuel.get({},function(datas){
             vm.labels=datas.months;
             vm.datas=datas.datas;
             vm.series=datas.labels;
         });*/



        $scope.$watch('vm.uoAcquereurId', function () {
            if (vm.uoAcquereurId != null) {
                getBilanByCountry(vm.uoAcquereurId);
            }
        });

        //   getBilanByCountry(3);

        function getBilanByCountry(uoId) {
            RequestUtils.BilanOpByCountryAndOp.get({
                uoId: uoId
            }, function (datas) {
                vm.labelsOpByCountry = datas.labels;
                vm.dataOpByCountry = datas.datas;
            });
        }

        RequestUtils.CompteAutreByEtiquette.get({
            etiquette: "CPTEOPERATIONACTEUR"
        }, function (datas) {
            if (datas.solde != null) {
                vm.compteOperationActeurSolde = datas.solde;
            } else {
                vm.compteOperationActeurSolde = 0;
            }
        });
        RequestUtils.CompteAutreByEtiquette.get({
            etiquette: "CPTECOMACTEUR"
        }, function (datas) {
            if (datas.solde != null) {
                vm.compteComActeurSolde = datas.solde;
            } else {
                vm.compteComActeurSolde = 0;
            }
        });
        //Recherche des dernières opérations
        function init() {
            Operation.queryByTop10({
                "size": 10,
                "statutOperation": vm.statutOperation
            }, function (datas) {
                vm.operations = datas;
                console.log('op-------------',vm.operations);
            });

            RequestUtils.GetOrganisationByType.get({
                type: "PAYS"
            }, function (datas) {
                vm.pays = datas;
            });
        }


        init();

        $scope.$watch("vm.statutOperation", function (value) {
            init();
        });

        $scope.$on('authenticationSuccess', function () {
            getAccount();
        });
        // $state.reload("app");
        getAccount();


        function getAccount() {

            Principal.identity().then(function (account) {
                vm.account = account;
                vm.isAuthenticated = Principal.isAuthenticated;
                if (vm.account===null){
                    $state.go('login')
                }
                if (account.resetKey != null) {
                    $state.go("finishReset", {
                        key: account.resetKey
                    });
                } else {
                    vm.isAuthenticated = null;
                }
                /*if(account.resetKey==null){
                    vm.isAuthenticated = Principal.isAuthenticated;
                }else{
                    $state.go('requestReset');
                    vm.isAuthenticated = null;
                }*/
            });
        }
        
        function loadImages() {
            console.log('spinner',$scope.images);
            $scope.loader = true;
            setTimeout(function () {
                $scope.$apply(function(){
                    $scope.loader = false;
                });
            }, 2000);
            $scope.images = true;
        }

        function startDashBoard() {
            vm.tableau=true;
            vm.dashB=false;
            initCanvasData();
            RequestUtils.BilanOp.get({}, function (datas) {
                console.log('1.1');
                vm.labelsOp = datas.labels;
                vm.dataOp = datas.datas;

                console.log('vm.dataOp----',vm.dataOp);
                console.log('vm.labelsOp----',vm.labelsOp);
            });

        }
        //startDashBoard();
        function spinnerLoad() {
            vm.tableau=true;
            startDashBoard();
            console.log('Operat');
            $uibModal.open({
                templateUrl: 'app/home/spinner-pop-up.html',
                controller: 'SpinnerPopUpController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                }
            }).result.then( function (result) {
                console.log('Operation 88',vm.dataOp);
                console.log('vm.periode',vm.labelsPeriode);
                console.log('vm.periode',vm.dataPeriode);
                vm.dashB=false;
            }, function() {
                console.log('start');

            });


            console.log('ff',vm.dataOp);

        }

        /* function register () {
             $state.go('register');
         }*/
    }
})();
